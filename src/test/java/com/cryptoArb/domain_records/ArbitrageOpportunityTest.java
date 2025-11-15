package com.cryptoArb.domain_records;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        /* Old non builder , record code
        // When
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                pair,
                timestamp,
                buyExchange,
                buyPrice,
                sellExchange,
                sellPrice
        );

        */

        // When
        // Use the new Builder to fix the test
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity.Builder()
                .pair(pair)
                .timestamp(timestamp)
                .buyExchange(buyExchange)
                .buyPrice(buyPrice)
                .sellExchange(sellExchange)
                .sellPrice(sellPrice)
                .build();


        // Then
        assertNotNull(opportunity);
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
//        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
//                new CurrencyPair("ETH", "USD"),
//                Instant.now(),
//                new Exchange("coinbase"),
//                new BigDecimal("2999"), // buyPrice
//                new Exchange("kraken"),
//                new BigDecimal("3000"));  // sellPrice

        // Given
        CurrencyPair pair = new CurrencyPair("ETH", "USD");
        Instant timestamp = Instant.now(); // 3. Changed from long

        Exchange buyExchange = new Exchange("coinbase"); //"kraken";
        BigDecimal buyPrice = new BigDecimal("2999.00"); // Buy low
        Exchange sellExchange = new Exchange("kraken");
        BigDecimal sellPrice = new BigDecimal("3000.00"); // Sell high



        ArbitrageOpportunity opportunity = new ArbitrageOpportunity.Builder()
                .pair(pair)
                .timestamp(timestamp)
                .buyExchange(buyExchange)
                .buyPrice(buyPrice)
                .sellExchange(sellExchange)
                .sellPrice(sellPrice)
                .build();


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


    @Test
    @DisplayName("Should create an opportunity using the Builder pattern")
    void shouldCreateOpportunityWithBuilder() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Instant timestamp = Instant.now();
        Exchange buyExchange = new Exchange("kraken");
        BigDecimal buyPrice = new BigDecimal("50000");
        Exchange sellExchange = new Exchange("coinbase");
        BigDecimal sellPrice = new BigDecimal("50001");

        // When
        // 1. ArbitrageOpportunity.Builder does not exist
        // 2. The methods .pair(), .buyPrice(), etc. do not exist
        // 3. The .build() method does not exist
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity.Builder()
                .pair(pair)
                .timestamp(timestamp)
                .buyExchange(buyExchange)
                .buyPrice(buyPrice)
                .sellExchange(sellExchange)
                .sellPrice(sellPrice)
                .build();

        // Then
        assertEquals(pair, opportunity.pair());
        assertEquals(timestamp, opportunity.timestamp());
        assertEquals(buyExchange, opportunity.buyExchange());
        assertEquals(buyPrice, opportunity.buyPrice());
        assertEquals(sellExchange, opportunity.sellExchange());
        assertEquals(sellPrice, opportunity.sellPrice());

        // We also need to verify that the profit calculation still happens
        // (3.2082e-05) -> 0.00002
        BigDecimal expectedProfit = new BigDecimal("0.00002");
        assertEquals(
                expectedProfit.setScale(5, RoundingMode.HALF_UP),
                opportunity.profitPercentage().setScale(5, RoundingMode.HALF_UP)
        );
    }
}