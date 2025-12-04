package com.cryptoArb.crypto_price_aggregator;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RED Test: Integration test expecting PostgreSQL connection.
 * This test will FAIL initially because:
 * 1. No PostgreSQL driver in dependencies
 * 2. No PostgreSQL Docker container configured
 * 3. No application-prod.properties with PostgreSQL config
 * 
 * Following TDD: This test defines our expectations before implementation.
 * 
 * NOW: After adding dependencies and Testcontainers, this should pass (GREEN
 * phase)
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PostgreSQLIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PriceTickRepository priceTickRepository;

    @BeforeEach
    public void cleanup() {
        priceTickRepository.deleteAll();
    }

    /**
     * RED: This test expects PostgreSQL connection
     * Will fail because PostgreSQL is not yet configured
     */
    @Test
    public void shouldConnectToPostgreSQL() throws Exception {
        // Verify we're connected to PostgreSQL database
        try (Connection connection = dataSource.getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            assertThat(databaseProductName).isEqualTo("PostgreSQL");
        }
    }

    /**
     * RED: This test expects to save and retrieve data from PostgreSQL
     * Will fail because PostgreSQL is not yet configured
     */
    @Test
    public void shouldPersistAndRetrievePriceTicks() {
        // Given: A price tick
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange exchange = Exchange.BINANCE;
        PriceTick tick = new PriceTick(
                pair,
                exchange,
                new BigDecimal("50000.00"),
                new BigDecimal("50001.00"),
                Instant.now());

        // When: We save it to the database
        PriceTick savedTick = priceTickRepository.save(tick);

        // Then: It should be persisted and retrievable
        assertThat(savedTick.getId()).isNotNull();

        PriceTick retrievedTick = priceTickRepository.findById(savedTick.getId()).orElseThrow();
        assertThat(retrievedTick.getPair().getBase()).isEqualTo("BTC");
        assertThat(retrievedTick.getPair().getQuote()).isEqualTo("USD");
        assertThat(retrievedTick.getExchange()).isEqualTo(Exchange.BINANCE);
        assertThat(retrievedTick.getBid()).isEqualByComparingTo("50000.00");
        assertThat(retrievedTick.getAsk()).isEqualByComparingTo("50001.00");
    }

    /**
     * RED: This test verifies PostgreSQL supports our query operations
     * Will fail because PostgreSQL is not yet configured
     */
    @Test
    public void shouldQueryPriceTicksByPairAndTimestamp() {
        // Given: Multiple price ticks
        CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
        CurrencyPair ethUsd = new CurrencyPair("ETH", "USD");
        Exchange binance = Exchange.BINANCE;

        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Instant twoHoursAgo = now.minusSeconds(7200);

        priceTickRepository
                .save(new PriceTick(btcUsd, binance, new BigDecimal("50000"), new BigDecimal("50001"), twoHoursAgo));
        priceTickRepository
                .save(new PriceTick(btcUsd, binance, new BigDecimal("51000"), new BigDecimal("51001"), oneHourAgo));
        priceTickRepository.save(new PriceTick(btcUsd, binance, new BigDecimal("52000"), new BigDecimal("52001"), now));
        priceTickRepository.save(new PriceTick(ethUsd, binance, new BigDecimal("3000"), new BigDecimal("3001"), now));

        // When: We query for BTC/USD ticks after 1.5 hours ago (between 1 hour and 2
        // hours)
        // This should exclude the 2-hour-old tick but include the 1-hour-old and
        // current ticks
        Instant oneAndHalfHoursAgo = now.minusSeconds(5400); // 1.5 hours = 5400 seconds
        List<PriceTick> recentTicks = priceTickRepository.findByPair_BaseAndPair_QuoteAndTimestampAfter(
                "BTC", "USD", oneAndHalfHoursAgo);

        // Then: We should get only 2 BTC/USD ticks (1 hour ago and now, not the 2 hour
        // old one, not ETH)
        assertThat(recentTicks).hasSize(2);
        assertThat(recentTicks).allMatch(tick -> tick.getPair().getBase().equals("BTC"));
        assertThat(recentTicks).allMatch(tick -> tick.getTimestamp().isAfter(oneAndHalfHoursAgo));
    }
}
