package com.cryptoArb.crypto_price_aggregator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PriceTickTest {

    private final CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
    private final Instant now = Instant.now();

    @Test
    @DisplayName("Should create valid PriceTick with all fields")
    void shouldCreateValidPriceTick() {
        // Arrange
        BigDecimal bid = new BigDecimal("50000.00");
        BigDecimal ask = new BigDecimal("50100.00");

        // Act
        PriceTick tick = new PriceTick(btcUsd, Exchange.BINANCE, bid, ask, now);

        // Assert
        assertNotNull(tick);
        assertEquals(btcUsd, tick.pair());
        assertEquals(Exchange.BINANCE, tick.exchange());
        assertEquals(bid, tick.bid());
        assertEquals(ask, tick.ask());
        assertEquals(now, tick.timestamp());
    }

    @Test
    @DisplayName("Should throw exception when CurrencyPair is null")
    void shouldThrowExceptionWhenPairIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(null, Exchange.BINANCE,
                    new BigDecimal("50000"), new BigDecimal("50100"), now);
        });
        assertTrue(exception.getMessage().contains("CurrencyPair cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when Exchange is null")
    void shouldThrowExceptionWhenExchangeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, null,
                    new BigDecimal("50000"), new BigDecimal("50100"), now);
        });
        assertTrue(exception.getMessage().contains("Exchange cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when bestBid is null")
    void shouldThrowExceptionWhenBidIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, Exchange.BINANCE,
                    null, new BigDecimal("50100"), now);
        });
        assertTrue(exception.getMessage().contains("Bid price cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when bestAsk is null")
    void shouldThrowExceptionWhenAskIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, Exchange.BINANCE,
                    new BigDecimal("50000"), null, now);
        });
        assertTrue(exception.getMessage().contains("Ask price cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when timestamp is null")
    void shouldThrowExceptionWhenTimestampIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, Exchange.BINANCE,
                    new BigDecimal("50000"), new BigDecimal("50100"), null);
        });
        assertTrue(exception.getMessage().contains("Timestamp cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when bestBid is negative")
    void shouldThrowExceptionWhenBidIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, Exchange.BINANCE,
                    new BigDecimal("-100"), new BigDecimal("50100"), now);
        });
        assertTrue(exception.getMessage().contains("Bid price must be non-negative"));
    }

    @Test
    @DisplayName("Should throw exception when bestAsk is negative")
    void shouldThrowExceptionWhenAskIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, Exchange.BINANCE,
                    new BigDecimal("50000"), new BigDecimal("-100"), now);
        });
        assertTrue(exception.getMessage().contains("Ask price must be non-negative"));
    }

    @Test
    @DisplayName("Should throw exception when bestBid is greater than bestAsk")
    void shouldThrowExceptionWhenBidGreaterThanAsk() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceTick(btcUsd, Exchange.BINANCE,
                    new BigDecimal("50100"), new BigDecimal("50000"), now);
        });
        assertTrue(exception.getMessage().contains("Bid price")
                && exception.getMessage().contains("cannot be greater than bestAsk price"));
    }

    @Test
    @DisplayName("Should allow bestBid equal to bestAsk (spread of zero)")
    void shouldAllowBidEqualToAsk() {
        // This is valid in some scenarios (zero spread)
        BigDecimal price = new BigDecimal("50000.00");

        PriceTick tick = new PriceTick(btcUsd, Exchange.BINANCE, price, price, now);

        assertNotNull(tick);
        assertEquals(price, tick.bid());
        assertEquals(price, tick.ask());
    }

    @Test
    @DisplayName("Should allow zero prices (valid in some scenarios)")
    void shouldAllowZeroPrices() {
        BigDecimal zero = BigDecimal.ZERO;

        PriceTick tick = new PriceTick(btcUsd, Exchange.BINANCE, zero, zero, now);

        assertNotNull(tick);
        assertEquals(zero, tick.bid());
        assertEquals(zero, tick.ask());
    }
}
