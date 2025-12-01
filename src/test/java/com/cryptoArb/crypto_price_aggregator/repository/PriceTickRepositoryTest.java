package com.cryptoArb.crypto_price_aggregator.repository;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Cycle 1 - RED: Test for PriceTickRepository
 * Expected to FAIL initially because:
 * 1. PriceTickRepository doesn't exist yet
 * 2. PriceTick is not a JPA entity yet
 * 3. CurrencyPair is not embeddable yet
 */
@DataJpaTest
@DisplayName("PriceTickRepository - JPA Repository Tests")
class PriceTickRepositoryTest {

    @Autowired
    private PriceTickRepository repository;

    @Test
    @DisplayName("RED: Should save and retrieve a PriceTick")
    void shouldSaveAndRetrievePriceTick() {
        // Given: A valid PriceTick
        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = new PriceTick(
                pair,
                Exchange.BINANCE,
                new BigDecimal("50000.00"),
                new BigDecimal("50001.00"),
                Instant.now());

        // When: We save it to the database
        PriceTick saved = repository.save(tick);

        // Then: It should be persisted with an ID
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPair().getBase()).isEqualTo("BTC");
        assertThat(saved.getPair().getQuote()).isEqualTo("USD");
    }

    @Test
    @DisplayName("RED: Should find all saved PriceTicks")
    void shouldFindAllPriceTicks() {
        // Given: Multiple PriceTicks saved
        CurrencyPair btcUsd = CurrencyPair.of("BTC", "USD");
        CurrencyPair ethUsd = CurrencyPair.of("ETH", "USD");

        PriceTick tick1 = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50001"), Instant.now());
        PriceTick tick2 = new PriceTick(ethUsd, Exchange.COINBASE,
                new BigDecimal("3000"), new BigDecimal("3001"), Instant.now());

        repository.save(tick1);
        repository.save(tick2);

        // When: We retrieve all ticks
        List<PriceTick> allTicks = repository.findAll();

        // Then: Both should be present
        assertThat(allTicks).hasSize(2);
    }

    @Test
    @DisplayName("Should find ticks by currency pair")
    void shouldFindTicksByPair() {
        // Given: Multiple ticks for different pairs
        CurrencyPair btcUsd = CurrencyPair.of("BTC", "USD");
        CurrencyPair ethUsd = CurrencyPair.of("ETH", "USD");

        repository.save(new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50001"), Instant.now()));
        repository.save(new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50010"), new BigDecimal("50011"), Instant.now()));
        repository.save(new PriceTick(ethUsd, Exchange.BINANCE,
                new BigDecimal("3000"), new BigDecimal("3001"), Instant.now()));

        // When: We query for BTC/USD ticks
        List<PriceTick> btcTicks = repository.findByPair_BaseAndPair_Quote("BTC", "USD");

        // Then: Should return only BTC/USD ticks
        assertThat(btcTicks).hasSize(2);
        assertThat(btcTicks)
                .allMatch(tick -> tick.getPair().getBase().equals("BTC") && tick.getPair().getQuote().equals("USD"));
    }

    @Test
    @DisplayName("Should find recent ticks by pair and timestamp")
    void shouldFindRecentTicksByPairAndTimestamp() {
        // Given: Ticks at different times
        CurrencyPair btcUsd = CurrencyPair.of("BTC", "USD");
        Instant oldTime = Instant.now().minusSeconds(10);
        Instant recentTime = Instant.now();

        PriceTick oldTick = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50001"), oldTime);
        PriceTick recentTick = new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50100"), new BigDecimal("50101"), recentTime);

        repository.save(oldTick);
        repository.save(recentTick);

        // When: We query for ticks after 5 seconds ago
        Instant cutoff = Instant.now().minusSeconds(5);
        List<PriceTick> recentTicks = repository.findByPair_BaseAndPair_QuoteAndTimestampAfter(
                "BTC", "USD", cutoff);

        // Then: Should return only recent tick
        assertThat(recentTicks).hasSize(1);
        assertThat(recentTicks.get(0).getExchange()).isEqualTo(Exchange.COINBASE);
    }

    @Test
    @DisplayName("Should find ticks by exchange")
    void shouldFindTicksByExchange() {
        // Given: Ticks from different exchanges
        CurrencyPair btcUsd = CurrencyPair.of("BTC", "USD");
        CurrencyPair ethUsd = CurrencyPair.of("ETH", "USD");

        repository.save(new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50001"), Instant.now()));
        repository.save(new PriceTick(ethUsd, Exchange.BINANCE,
                new BigDecimal("3000"), new BigDecimal("3001"), Instant.now()));
        repository.save(new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50010"), new BigDecimal("50011"), Instant.now()));

        // When: We query for Binance ticks
        List<PriceTick> binanceTicks = repository.findByExchange(Exchange.BINANCE);

        // Then: Should return only Binance ticks
        assertThat(binanceTicks).hasSize(2);
        assertThat(binanceTicks).allMatch(tick -> tick.getExchange() == Exchange.BINANCE);
    }

    @Test
    @DisplayName("Should find recent ticks across all pairs with custom query")
    void shouldFindRecentTicksAcrossAllPairs() {
        // Given: Ticks at different times for different pairs
        Instant oldTime = Instant.now().minusSeconds(10);
        Instant recentTime = Instant.now();

        repository.save(new PriceTick(CurrencyPair.of("BTC", "USD"), Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50001"), oldTime));
        repository.save(new PriceTick(CurrencyPair.of("ETH", "USD"), Exchange.COINBASE,
                new BigDecimal("3000"), new BigDecimal("3001"), recentTime));
        repository.save(new PriceTick(CurrencyPair.of("BTC", "USD"), Exchange.KRAKEN,
                new BigDecimal("50100"), new BigDecimal("50101"), recentTime));

        // When: We query for recent ticks (last 5 seconds)
        Instant cutoff = Instant.now().minusSeconds(5);
        List<PriceTick> recentTicks = repository.findRecentTicks(cutoff);

        // Then: Should return only recent ticks, ordered by timestamp DESC
        assertThat(recentTicks).hasSize(2);
        assertThat(recentTicks.get(0).getTimestamp()).isAfterOrEqualTo(recentTicks.get(1).getTimestamp());
    }

    @Test
    @DisplayName("Should return empty list when no ticks match query")
    void shouldReturnEmptyListWhenNoMatches() {
        // Given: No ticks in repository

        // When: We query for non-existent pair
        List<PriceTick> results = repository.findByPair_BaseAndPair_Quote("XRP", "EUR");

        // Then: Should return empty list
        assertThat(results).isEmpty();
    }
}
