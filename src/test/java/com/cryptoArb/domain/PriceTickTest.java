package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PriceTickTest {

    @Test
    @DisplayName("Should create a PriceTick_old and verify all its properties")
    void givenTickData_whenCreatePriceTick_thenPropertiesAreSet() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange exchange = new Exchange("coinbase");
        long timestamp = Instant.now().toEpochMilli();
        double bidPrice = 50000.00;
        double askPrice = 50000.50;

        // When
        PriceTick tick = new PriceTick(pair, exchange, timestamp, bidPrice, askPrice);

        // Then: Use the new accessor methods
        assertEquals(pair, tick.pair(), "CurrencyPair_old should be set");
        assertEquals(exchange, tick.exchange(), "Exchange_old should be set");
        assertEquals(timestamp, tick.timestamp(), "Timestamp should be set");
        assertEquals(bidPrice, tick.bidPrice(), "Bid price should be set");
        assertEquals(askPrice, tick.askPrice(), "Ask price should be set");
    }

}