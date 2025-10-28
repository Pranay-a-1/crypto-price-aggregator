package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArbitrageOpportunityTest {

    @Test
    @DisplayName("Should create an ArbitrageOpportunity and verify its properties")
    void givenOppData_whenCreateArbitrageOpportunity_thenPropertiesAreSet() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        long timestamp = Instant.now().toEpochMilli();

        String buyExchange = "kraken";
        BigDecimal buyPrice = new BigDecimal("49999.00"); // Buy low
        String sellExchange = "coinbase";
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
                sellPrice,
                profitPercentage
        );

        // Then
        assertEquals(pair, opportunity.pair());
        assertEquals(timestamp, opportunity.timestamp());
        assertEquals(buyExchange, opportunity.buyExchange());
        assertEquals(buyPrice, opportunity.buyPrice());
        assertEquals(sellExchange, opportunity.sellExchange());
        assertEquals(sellPrice, opportunity.sellPrice());
        assertEquals(profitPercentage, opportunity.profitPercentage());
    }
}