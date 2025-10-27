package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PriceTick_2_recordTest {

    @Test
    @DisplayName("Should create a PriceTick and verify all its properties")
    void givenTickData_whenCreatePriceTick_thenPropertiesAreSet() {
        // Given
        CurrencyPair_2_record pair = new CurrencyPair_2_record("BTC", "USD");
        Exchange_2_record exchange = new Exchange_2_record("coinbase");
        long timestamp = Instant.now().toEpochMilli();
        double bidPrice = 50000.00;
        double askPrice = 50000.50;

        // When
        PriceTick_2_record tick = new PriceTick_2_record(pair, exchange, timestamp, bidPrice, askPrice);

        // Then: Use the new accessor methods
        assertEquals(pair, tick.pair(), "CurrencyPair should be set");
        assertEquals(exchange, tick.exchange(), "Exchange should be set");
        assertEquals(timestamp, tick.timestamp(), "Timestamp should be set");
        assertEquals(bidPrice, tick.bidPrice(), "Bid price should be set");
        assertEquals(askPrice, tick.askPrice(), "Ask price should be set");
    }

}