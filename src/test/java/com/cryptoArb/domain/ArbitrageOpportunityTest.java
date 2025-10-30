package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArbitrageOpportunityTest {

    @Test
    @DisplayName("Should create an ArbitrageOpportunity and verify its properties")
    void givenOppData_whenCreateArbitrageOpportunity_thenPropertiesAreSet() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Instant timestamp = Instant.now(); // 3. Changed from long

        Exchange buyExchange = new Exchange("kraken"); //"kraken";
        BigDecimal buyPrice = new BigDecimal("49999.00"); // Buy low
        Exchange sellExchange = new Exchange("coinbase");
        BigDecimal sellPrice = new BigDecimal("50001.00"); // Sell high
        BigDecimal profitPercentage = new BigDecimal("0.004"); // (50001 - 49999) / 49999

        // When
        // This line will fail to compile (RED)
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                pair,
                timestamp,
                buyExchange,
                buyPrice,
                sellExchange,
                sellPrice
        );

        // Then
        assertEquals(pair, opportunity.pair());
        assertEquals(timestamp, opportunity.timestamp());
        assertEquals(buyExchange, opportunity.buyExchange());
        assertEquals(buyPrice, opportunity.buyPrice());
        assertEquals(sellExchange, opportunity.sellExchange());
        assertEquals(sellPrice, opportunity.sellPrice());
    }


    @Test
    @DisplayName("Should correctly calculate the profit percentage")
    void givenPrices_whenCalculateProfit_thenReturnsCorrectPercentage() {
        // Given: Buy at 2999, Sell at 3000
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                new CurrencyPair("ETH", "USD"),
                Instant.now(),
                new Exchange("coinbase"),
                new BigDecimal("2999"), // buyPrice
                new Exchange("kraken"),
                new BigDecimal("3000"));  // sellPrice

        // (3000 - 2999) / 2999 = 1 / 2999 = 0.0003334...
        // Our test expects 0.0333 (as a percentage * 100, that's 0.0333%)
        BigDecimal expectedProfit = new BigDecimal("0.00033344");

        // When
        BigDecimal actualProfit = opportunity.profitPercentage();

        // Then
        // We scale the result for comparison, just like in our service test
        assertEquals(
                expectedProfit.setScale(8, RoundingMode.HALF_UP),
                actualProfit.setScale(8, RoundingMode.HALF_UP)
        );
    }
}