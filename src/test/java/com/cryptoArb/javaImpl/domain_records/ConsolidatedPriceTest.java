package com.cryptoArb.javaImpl.domain_records;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsolidatedPriceTest {


    @Test
    @DisplayName("Should create a ConsolidatedPrice and verify its properties")
    void givenPriceData_whenCreateConsolidatedPrice_thenPropertiesAreSet() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Instant timestamp = Instant.now();
        BigDecimal bestBid = new BigDecimal("50000.00");
        Exchange bestBidExchange = new Exchange("kraken");
        BigDecimal bestAsk = new BigDecimal("50001.50");
        Exchange bestAskExchange = new Exchange("coinbase");

        // When
        ConsolidatedPrice price = new ConsolidatedPrice(
                pair,
                timestamp,
                bestBid,
                bestBidExchange,
                bestAsk,
                bestAskExchange
        );

        // Then
        assertEquals(pair, price.pair());
        assertEquals(timestamp, price.timestamp());
        assertEquals(bestBid, price.bestBid());
        assertEquals(bestBidExchange, price.bestBidExchange());
        assertEquals(bestAsk, price.bestAsk());
        assertEquals(bestAskExchange, price.bestAskExchange());
    }
}