package com.cryptoArb.integration;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
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

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Phase 14: Dead Letter Queue Integration Test.
 * 
 * TDD RED PHASE: This test demonstrates the DLQ flow.
 * 
 * Test Strategy:
 * 1. Send an invalid PriceTick that will cause permanent failure
 * 2. Consumer will retry 3 times (per retry policy)
 * 3. After max retries, message is rejected and routed to DLQ
 * 4. DLQ listener receives and logs the failed message
 * 
 * Why This Test:
 * - Validates complete retry + DLQ mechanism
 * - Proves failures don't crash the system
 * - Shows proper error handling in production scenarios
 */
@SpringBootTest(classes = CryptoPriceAggregatorApplication.class)
@Testcontainers
class RabbitMqDlqIntegrationTest {

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

        @org.springframework.boot.test.mock.mockito.MockBean
        private com.cryptoArb.scheduler.PriceFetchScheduler priceFetchScheduler;

        @Value("${cpa.rabbitmq.exchange}")
        private String exchangeName;

        @Value("${cpa.rabbitmq.routing-key}")
        private String routingKey;

        @Value("${cpa.rabbitmq.dlq.queue}")
        private String dlqName;

        @Autowired
        private com.cryptoArb.repository.PriceTickRepository priceTickRepository;

        @Autowired
        private com.cryptoArb.repository.ArbitrageRepository arbitrageRepository;

        @Autowired
        private io.micrometer.core.instrument.MeterRegistry meterRegistry;

        @BeforeEach
        void setUp() {
                // Clean up database before each test
                arbitrageRepository.deleteAll();
                priceTickRepository.deleteAll();
        }

        @Test
        @DisplayName("Should route permanently failed message to DLQ after max retries")
        void testMessageRoutedToDlqAfterFailure() {
                // Given: Record initial DLQ message count
                double initialDlqCount = meterRegistry.counter("rabbitmq.dlq.messages", "queue", "price.tick.dlq")
                                .count();

                // Given: An invalid PriceTick that will cause processing to fail
                // Using null CurrencyPair to trigger validation failure
                PriceTick invalidTick = new PriceTick(
                                null, // This will cause validation failure
                                new Exchange("test-exchange"),
                                Instant.now(),
                                new BigDecimal("50000.00"),
                                new BigDecimal("50010.00"));

                // When: We send the invalid message to RabbitMQ
                rabbitTemplate.convertAndSend(exchangeName, routingKey, invalidTick);

                // Then: After retries are exhausted, message should be processed by DLQ
                // listener
                // The consumer will retry 3 times (per RetryTemplate config)
                // After max retries, message is rejected and routed to DLQ
                // The DLQ listener will consume it and increment the metric
                //
                // Expected Timeline:
                // - t=0s: First attempt (fails, retries internally)
                // - After 3 retry attempts: Message rejected, routed to DLQ
                // - DLQ listener consumes message immediately
                // - DLQ metric counter incremented
                //
                // We verify by checking that DLQ counter increased
                await().atMost(15, SECONDS)
                                .pollInterval(1, SECONDS)
                                .untilAsserted(() -> {
                                        // Get current DLQ message count
                                        double currentDlqCount = meterRegistry
                                                        .counter("rabbitmq.dlq.messages", "queue", "price.tick.dlq")
                                                        .count();

                                        // Assert: DLQ counter should have increased by 1
                                        org.assertj.core.api.Assertions.assertThat(currentDlqCount)
                                                        .as("DLQ metric should increment after failed message is routed to DLQ and consumed")
                                                        .isEqualTo(initialDlqCount + 1);
                                });

                // And: The database should still be empty (message was never successfully
                // processed)
                org.assertj.core.api.Assertions.assertThat(priceTickRepository.count())
                                .as("No PriceTick should be saved for invalid message")
                                .isEqualTo(0L);
        }

        @Test
        @DisplayName("Should log comprehensive failure information in DLQ listener")
        void testDlqListenerLogsFailureDetails() {
                // Given: Record initial DLQ message count
                double initialDlqCount = meterRegistry.counter("rabbitmq.dlq.messages", "queue", "price.tick.dlq")
                                .count();

                // Given: An invalid PriceTick
                PriceTick invalidTick = new PriceTick(
                                null, // null pair triggers validation failure
                                new Exchange("binance"),
                                Instant.now(),
                                new BigDecimal("3000.00"),
                                new BigDecimal("3005.00"));

                // When: We send the message
                rabbitTemplate.convertAndSend(exchangeName, routingKey, invalidTick);

                // Then: DLQ listener should process it and increment the metric
                // We verify by waiting for DLQ counter to increase
                await().atMost(15, SECONDS)
                                .untilAsserted(() -> {
                                        double currentDlqCount = meterRegistry
                                                        .counter("rabbitmq.dlq.messages", "queue", "price.tick.dlq")
                                                        .count();

                                        org.assertj.core.api.Assertions.assertThat(currentDlqCount)
                                                        .as("DLQ listener should have processed the failed message")
                                                        .isGreaterThanOrEqualTo(initialDlqCount + 1);
                                });

                // Note: Actual log verification would require a LogCaptor
                // For now, we verify the message was processed by DLQ listener via metric
                // Manual verification: Check logs for DLQ listener output
        }
}
