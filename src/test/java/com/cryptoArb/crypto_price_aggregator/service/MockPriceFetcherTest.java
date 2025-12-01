package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class MockPriceFetcherTest {

    private MockPriceFetcher fetcher;
    private CurrencyPair btcUsd;

    @BeforeEach
    void setUp() {
        fetcher = new MockPriceFetcher(Exchange.BINANCE);
        btcUsd = new CurrencyPair("BTC", "USD");
    }

    @Test
    @DisplayName("Should return non-null PriceTick for valid pair")
    void shouldReturnNonNullPriceTick() throws PriceFetchException {
        // Act
        PriceTick tick = fetcher.fetchPrice(btcUsd);

        // Assert
        assertNotNull(tick, "Fetched tick should not be null");
    }

    @Test
    @DisplayName("Should return PriceTick with correct exchange")
    void shouldReturnPriceTickWithCorrectExchange() throws PriceFetchException {
        // Act
        PriceTick tick = fetcher.fetchPrice(btcUsd);

        // Assert
        assertEquals(Exchange.BINANCE, tick.getExchange(),
                "Tick should be from BINANCE exchange");
    }

    @Test
    @DisplayName("Should return PriceTick with correct currency pair")
    void shouldReturnPriceTickWithCorrectPair() throws PriceFetchException {
        // Act
        PriceTick tick = fetcher.fetchPrice(btcUsd);

        // Assert
        assertEquals(btcUsd, tick.getPair(),
                "Tick should have the requested currency pair");
    }

    @Test
    @DisplayName("Should ensure bestBid is less than bestAsk (valid spread)")
    void shouldEnsureBidLessThanAsk() throws PriceFetchException {
        // Act
        PriceTick tick = fetcher.fetchPrice(btcUsd);

        // Assert
        assertTrue(tick.getBid().compareTo(tick.getAsk()) < 0,
                "Bid should be less than bestAsk (positive spread)");
    }

    @Test
    @DisplayName("Should return positive prices")
    void shouldReturnPositivePrices() throws PriceFetchException {
        // Act
        PriceTick tick = fetcher.fetchPrice(btcUsd);

        // Assert
        assertTrue(tick.getBid().compareTo(BigDecimal.ZERO) > 0,
                "Bid should be positive");
        assertTrue(tick.getAsk().compareTo(BigDecimal.ZERO) > 0,
                "Ask should be positive");
    }

    @Test
    @DisplayName("Should have recent timestamp")
    void shouldHaveRecentTimestamp() throws PriceFetchException {
        // Act
        PriceTick tick = fetcher.fetchPrice(btcUsd);

        // Assert
        assertNotNull(tick.getTimestamp(), "Timestamp should not be null");
        // Timestamp should be within the last few seconds
        assertTrue(Duration.between(tick.getTimestamp(),
                Instant.now()).getSeconds() < 5,
                "Timestamp should be recent (within 5 seconds)");
    }

    @Test
    @DisplayName("Should throw exception for null currency pair")
    void shouldThrowExceptionForNullPair() {
        // Act & Assert
        PriceFetchException exception = assertThrows(PriceFetchException.class, () -> {
            fetcher.fetchPrice(null);
        });

        assertTrue(exception.getMessage().contains("CurrencyPair cannot be null"));
    }

    @Test
    @DisplayName("Should return different prices on multiple calls (randomness)")
    void shouldReturnDifferentPricesOnMultipleCalls() throws PriceFetchException {
        // Act
        PriceTick tick1 = fetcher.fetchPrice(btcUsd);
        PriceTick tick2 = fetcher.fetchPrice(btcUsd);

        // Assert
        // There's a very small chance they could be equal, but extremely unlikely
        assertNotEquals(tick1.getBid(), tick2.getBid(),
                "Different calls should generate different prices");
    }

    @Test
    @DisplayName("Default constructor should use MOCK exchange")
    void defaultConstructorShouldUseMockExchange() throws PriceFetchException {
        // Arrange
        MockPriceFetcher defaultFetcher = new MockPriceFetcher();

        // Act
        PriceTick tick = defaultFetcher.fetchPrice(btcUsd);

        // Assert
        assertEquals(Exchange.MOCK, tick.getExchange());
        assertEquals(Exchange.MOCK, defaultFetcher.getExchange());
    }

    @Test
    @DisplayName("getExchange should return configured exchange")
    void getExchangeShouldReturnConfiguredExchange() {
        // Arrange
        MockPriceFetcher binanceFetcher = new MockPriceFetcher(Exchange.BINANCE);
        MockPriceFetcher coinbaseFetcher = new MockPriceFetcher(Exchange.COINBASE);

        // Assert
        assertEquals(Exchange.BINANCE, binanceFetcher.getExchange());
        assertEquals(Exchange.COINBASE, coinbaseFetcher.getExchange());
    }
}
