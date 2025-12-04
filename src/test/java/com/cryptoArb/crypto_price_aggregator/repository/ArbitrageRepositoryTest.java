package com.cryptoArb.crypto_price_aggregator.repository;

import com.cryptoArb.crypto_price_aggregator.BaseIntegrationTest;
import com.cryptoArb.crypto_price_aggregator.domain.ArbitrageOpportunity;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Integration tests for ArbitrageRepository.
 * 
 * Extends BaseIntegrationTest to use Testcontainers with PostgreSQL.
 * Tests the repository layer with real database queries.
 * 
 * Following TDD: Write tests first (RED), implement repository (GREEN),
 * refactor.
 */
class ArbitrageRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private ArbitrageRepository arbitrageRepository;

    /**
     * Clean up test data before each test to ensure isolation.
     * This ensures each test starts with an empty database.
     */
    @BeforeEach
    void setUp() {
        arbitrageRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveArbitrageOpportunity() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                pair,
                Exchange.BINANCE,
                Exchange.COINBASE,
                new BigDecimal("50000.00"),
                new BigDecimal("50500.00"),
                new BigDecimal("1.00"),
                Instant.now());

        // When
        ArbitrageOpportunity saved = arbitrageRepository.save(opportunity);

        // Then
        assertNotNull(saved.getId());
        ArbitrageOpportunity retrieved = arbitrageRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(pair, retrieved.getCurrencyPair());
        assertEquals(Exchange.BINANCE, retrieved.getBuyExchange());
        assertEquals(Exchange.COINBASE, retrieved.getSellExchange());
    }

    @Test
    void shouldFindByCurrencyPairOrderedByDetectedAt() {
        // Given - Multiple opportunities for same pair at different times
        CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
        CurrencyPair ethUsd = new CurrencyPair("ETH", "USD");

        // PostgreSQL truncates timestamps to microseconds, so we need to do the same
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant earlier = now.minus(1, ChronoUnit.HOURS);
        Instant latest = now.plus(1, ChronoUnit.HOURS);

        ArbitrageOpportunity opp1 = createOpportunity(btcUsd, earlier);
        ArbitrageOpportunity opp2 = createOpportunity(btcUsd, latest);
        ArbitrageOpportunity opp3 = createOpportunity(btcUsd, now);
        ArbitrageOpportunity oppEth = createOpportunity(ethUsd, now);

        arbitrageRepository.save(opp1);
        arbitrageRepository.save(opp2);
        arbitrageRepository.save(opp3);
        arbitrageRepository.save(oppEth);

        // When
        List<ArbitrageOpportunity> btcOpportunities = arbitrageRepository
                .findByCurrencyPair_BaseAndCurrencyPair_QuoteOrderByDetectedAtDesc("BTC", "USD");

        // Then
        assertEquals(3, btcOpportunities.size());
        // Should be ordered by detectedAt DESC (latest first)
        assertEquals(latest, btcOpportunities.get(0).getDetectedAt());
        assertEquals(now, btcOpportunities.get(1).getDetectedAt());
        assertEquals(earlier, btcOpportunities.get(2).getDetectedAt());
    }

    @Test
    void shouldFindByDetectedAtAfter() {
        // Given - Opportunities at different times
        Instant now = Instant.now();
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant thirtyMinutesAgo = now.minus(30, ChronoUnit.MINUTES);

        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        arbitrageRepository.save(createOpportunity(pair, twoHoursAgo));
        arbitrageRepository.save(createOpportunity(pair, oneHourAgo));
        arbitrageRepository.save(createOpportunity(pair, thirtyMinutesAgo));

        // When - find opportunities detected after 1.5 hours ago
        Instant cutoff = now.minus(90, ChronoUnit.MINUTES);
        List<ArbitrageOpportunity> recentOpportunities = arbitrageRepository.findByDetectedAtAfter(cutoff);

        // Then - should only find the one from 1 hour ago and 30 minutes ago
        assertEquals(2, recentOpportunities.size());
        assertTrue(recentOpportunities.stream()
                .allMatch(opp -> opp.getDetectedAt().isAfter(cutoff)));
    }

    @Test
    void shouldReturnEmptyListWhenNoCurrencyPairMatches() {
        // Given - Save opportunity for BTC/USD
        CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
        arbitrageRepository.save(createOpportunity(btcUsd, Instant.now()));

        // When - Query for ETH/USD
        List<ArbitrageOpportunity> opportunities = arbitrageRepository
                .findByCurrencyPair_BaseAndCurrencyPair_QuoteOrderByDetectedAtDesc("ETH", "USD");

        // Then
        assertTrue(opportunities.isEmpty());
    }

    @Test
    void shouldSaveMultipleOpportunitiesWithDifferentExchanges() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Instant now = Instant.now();

        ArbitrageOpportunity opp1 = new ArbitrageOpportunity(
                pair, Exchange.BINANCE, Exchange.COINBASE,
                new BigDecimal("50000"), new BigDecimal("50500"),
                new BigDecimal("1.00"), now);

        ArbitrageOpportunity opp2 = new ArbitrageOpportunity(
                pair, Exchange.KRAKEN, Exchange.BINANCE,
                new BigDecimal("49900"), new BigDecimal("50300"),
                new BigDecimal("0.80"), now);

        // When
        arbitrageRepository.save(opp1);
        arbitrageRepository.save(opp2);

        // Then
        List<ArbitrageOpportunity> all = arbitrageRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    // Helper method to create test opportunities
    private ArbitrageOpportunity createOpportunity(CurrencyPair pair, Instant detectedAt) {
        return new ArbitrageOpportunity(
                pair,
                Exchange.BINANCE,
                Exchange.COINBASE,
                new BigDecimal("50000.00"),
                new BigDecimal("50500.00"),
                new BigDecimal("1.00"),
                detectedAt);
    }
}
