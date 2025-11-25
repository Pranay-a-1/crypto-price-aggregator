package com.cryptoArb.integration;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.repository.ArbitrageRepository;
import com.cryptoArb.repository.PriceTickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Phase 14: RabbitMQ Integration Test.
 * 
 * This test validates the complete message flow:
 * 1. Producer sends PriceTick to RabbitMQ
 * 2. Consumer (@RabbitListener) receives and processes the message
 * 3. PriceTick is saved to database
 * 4. Arbitrage detection logic runs
 * 5. ArbitrageOpportunity is saved to database (if detected)
 * 
 * Uses Testcontainers for RabbitMQ and Redis.
 */
@SpringBootTest(classes = CryptoPriceAggregatorApplication.class)
@Testcontainers
class RabbitMqMessagingIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.13-management"));

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PriceTickRepository priceTickRepository;

    @Autowired
    private ArbitrageRepository arbitrageRepository;

    @Value("${cpa.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${cpa.rabbitmq.routing-key}")
    private String routingKey;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        arbitrageRepository.deleteAll();
        priceTickRepository.deleteAll();
    }

    @Test
    @DisplayName("Should process PriceTick message and save to database")
    void testMessageProcessing() {
        // Given: A PriceTick message
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange exchange = new Exchange("coinbase");
        PriceTick tick = new PriceTick(
                pair,
                exchange,
                Instant.now(),
                new BigDecimal("50000.00"),
                new BigDecimal("50010.00"));

        // When: We send the message to RabbitMQ
        rabbitTemplate.convertAndSend(exchangeName, routingKey, tick);

        // Then: The message should be consumed and saved to database
        await().atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<PriceTick> savedTicks = priceTickRepository.findAll();
                    assertThat(savedTicks).hasSize(1);

                    PriceTick savedTick = savedTicks.get(0);
                    assertThat(savedTick.getPair().getBase()).isEqualTo("BTC");
                    assertThat(savedTick.getPair().getQuote()).isEqualTo("USD");
                    assertThat(savedTick.getExchange().getId()).isEqualTo("coinbase");
                    assertThat(savedTick.getBidPrice()).isEqualByComparingTo("50000.00");
                    assertThat(savedTick.getAskPrice()).isEqualByComparingTo("50010.00");
                });
    }

    @Test
    @DisplayName("Should detect and save arbitrage opportunity")
    void testArbitrageDetection() {
        // Given: Two PriceTicks from different exchanges with arbitrage opportunity
        CurrencyPair pair = new CurrencyPair("ETH", "USD");

        // Exchange 1: Lower ask price (buy here)
        PriceTick tick1 = new PriceTick(
                pair,
                new Exchange("binance"),
                Instant.now(),
                new BigDecimal("3000.00"), // bid
                new BigDecimal("3005.00") // ask (lower - buy here)
        );

        // Exchange 2: Higher bid price (sell here)
        PriceTick tick2 = new PriceTick(
                pair,
                new Exchange("coinbase"),
                Instant.now(),
                new BigDecimal("3020.00"), // bid (higher - sell here)
                new BigDecimal("3025.00") // ask
        );

        // When: We send both messages to RabbitMQ
        rabbitTemplate.convertAndSend(exchangeName, routingKey, tick1);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, tick2);

        // Then: Both ticks should be saved
        await().atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<PriceTick> savedTicks = priceTickRepository.findAll();
                    assertThat(savedTicks).hasSize(2);
                });

        // And: An arbitrage opportunity should be detected and saved
        await().atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<ArbitrageOpportunity> opportunities = arbitrageRepository.findAll();
                    assertThat(opportunities).isNotEmpty();

                    ArbitrageOpportunity opportunity = opportunities.get(0);
                    assertThat(opportunity.getPair().getBase()).isEqualTo("ETH");
                    assertThat(opportunity.getPair().getQuote()).isEqualTo("USD");

                    // Buy at binance (lower ask)
                    assertThat(opportunity.getBuyExchange().getId()).isEqualTo("binance");
                    assertThat(opportunity.getBuyPrice()).isEqualByComparingTo("3005.00");

                    // Sell at coinbase (higher bid)
                    assertThat(opportunity.getSellExchange().getId()).isEqualTo("coinbase");
                    assertThat(opportunity.getSellPrice()).isEqualByComparingTo("3020.00");

                    // Profit should be positive
                    assertThat(opportunity.getProfitPercentage()).isGreaterThan(BigDecimal.ZERO);
                });
    }

    @Test
    @DisplayName("Should not create arbitrage opportunity when none exists")
    void testNoArbitrageWhenPricesOverlap() {
        // Given: Two PriceTicks with overlapping prices (no arbitrage)
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        PriceTick tick1 = new PriceTick(
                pair,
                new Exchange("binance"),
                Instant.now(),
                new BigDecimal("50000.00"),
                new BigDecimal("50010.00"));

        PriceTick tick2 = new PriceTick(
                pair,
                new Exchange("coinbase"),
                Instant.now(),
                new BigDecimal("50005.00"),
                new BigDecimal("50015.00"));

        // When: We send both messages
        rabbitTemplate.convertAndSend(exchangeName, routingKey, tick1);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, tick2);

        // Then: Both ticks should be saved
        await().atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<PriceTick> savedTicks = priceTickRepository.findAll();
                    assertThat(savedTicks).hasSize(2);
                });

        // And: No arbitrage opportunity should be created
        // Wait a bit to ensure consumer has processed
        await().pollDelay(2, SECONDS)
                .atMost(5, SECONDS)
                .untilAsserted(() -> {
                    List<ArbitrageOpportunity> opportunities = arbitrageRepository.findAll();
                    assertThat(opportunities).isEmpty();
                });
    }

    @Test
    @DisplayName("Should handle multiple messages concurrently")
    void testConcurrentMessageProcessing() {
        // Given: Multiple PriceTick messages
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        for (int i = 0; i < 10; i++) {
            PriceTick tick = new PriceTick(
                    pair,
                    new Exchange("exchange" + i),
                    Instant.now(),
                    new BigDecimal("50000.00").add(BigDecimal.valueOf(i)),
                    new BigDecimal("50010.00").add(BigDecimal.valueOf(i)));

            // When: We send messages rapidly
            rabbitTemplate.convertAndSend(exchangeName, routingKey, tick);
        }

        // Then: All messages should be processed and saved
        await().atMost(15, SECONDS)
                .untilAsserted(() -> {
                    List<PriceTick> savedTicks = priceTickRepository.findAll();
                    assertThat(savedTicks).hasSize(10);
                });
    }
}
