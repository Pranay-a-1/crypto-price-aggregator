package com.cryptoArb.crypto_price_aggregator.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Tests for ArbitrageOpportunity domain model.
 * 
 * Following TDD RED-GREEN-REFACTOR cycle:
 * - Tests written first (will fail initially)
 * - Minimal implementation to make tests pass
 * - Refactor for quality
 */
class ArbitrageOpportunityTest {

    @Test
    void shouldCreateValidArbitrageOpportunity() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.COINBASE;
        BigDecimal buyPrice = new BigDecimal("50000.00");
        BigDecimal sellPrice = new BigDecimal("50500.00");
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = Instant.now();

        // When
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt);

        // Then
        assertNotNull(opportunity);
        assertEquals(pair, opportunity.getCurrencyPair());
        assertEquals(buyExchange, opportunity.getBuyExchange());
        assertEquals(sellExchange, opportunity.getSellExchange());
        assertEquals(buyPrice, opportunity.getBuyPrice());
        assertEquals(sellPrice, opportunity.getSellPrice());
        assertEquals(profitPercentage, opportunity.getProfitPercentage());
        assertEquals(detectedAt, opportunity.getDetectedAt());
    }

    @Test
    void shouldThrowExceptionWhenBuyPriceGreaterThanSellPrice() {
        // Given - invalid scenario where buy price > sell price
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.COINBASE;
        BigDecimal buyPrice = new BigDecimal("50500.00"); // Higher
        BigDecimal sellPrice = new BigDecimal("50000.00"); // Lower - invalid!
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = Instant.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ArbitrageOpportunity(
                        pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt));

        assertTrue(exception.getMessage().contains("Buy price must be less than sell price"));
    }

    @Test
    void shouldThrowExceptionWhenProfitPercentageIsNegative() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.COINBASE;
        BigDecimal buyPrice = new BigDecimal("50000.00");
        BigDecimal sellPrice = new BigDecimal("50500.00");
        BigDecimal profitPercentage = new BigDecimal("-1.00"); // Invalid!
        Instant detectedAt = Instant.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ArbitrageOpportunity(
                        pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt));

        assertTrue(exception.getMessage().contains("Profit percentage must be positive"));
    }

    @Test
    void shouldThrowExceptionWhenCurrencyPairIsNull() {
        // Given
        CurrencyPair pair = null;
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.COINBASE;
        BigDecimal buyPrice = new BigDecimal("50000.00");
        BigDecimal sellPrice = new BigDecimal("50500.00");
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = Instant.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ArbitrageOpportunity(
                        pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt));

        assertTrue(exception.getMessage().contains("CurrencyPair cannot be null"));
    }

    @Test
    void shouldThrowExceptionWhenExchangesAreNull() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = null;
        Exchange sellExchange = null;
        BigDecimal buyPrice = new BigDecimal("50000.00");
        BigDecimal sellPrice = new BigDecimal("50500.00");
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = Instant.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ArbitrageOpportunity(
                        pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt));

        assertTrue(exception.getMessage().contains("Exchange cannot be null"));
    }

    @Test
    void shouldThrowExceptionWhenPricesAreNull() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.COINBASE;
        BigDecimal buyPrice = null;
        BigDecimal sellPrice = null;
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = Instant.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ArbitrageOpportunity(
                        pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt));

        assertTrue(exception.getMessage().contains("Price cannot be null"));
    }

    @Test
    void shouldThrowExceptionWhenTimestampIsNull() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.COINBASE;
        BigDecimal buyPrice = new BigDecimal("50000.00");
        BigDecimal sellPrice = new BigDecimal("50500.00");
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ArbitrageOpportunity(
                        pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt));

        assertTrue(exception.getMessage().contains("Timestamp cannot be null"));
    }

    @Test
    void shouldAllowSameExchangeForBuyAndSell() {
        // Given - edge case: same exchange (unusual but not invalid)
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange buyExchange = Exchange.BINANCE;
        Exchange sellExchange = Exchange.BINANCE; // Same exchange
        BigDecimal buyPrice = new BigDecimal("50000.00");
        BigDecimal sellPrice = new BigDecimal("50500.00");
        BigDecimal profitPercentage = new BigDecimal("1.00");
        Instant detectedAt = Instant.now();

        // When
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                pair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt);

        // Then
        assertNotNull(opportunity);
        assertEquals(buyExchange, opportunity.getSellExchange());
    }
}
