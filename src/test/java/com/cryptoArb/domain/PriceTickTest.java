package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceTickTest {

    @Test
    @DisplayName("Should create a PriceTick and verify all its properties")
    void givenTickData_whenCreatePriceTick_thenPropertiesAreSet() {
        // Given: All the data needed for a price tick
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Exchange exchange = new Exchange("coinbase");
        long timestamp = Instant.now().toEpochMilli();
        double bidPrice = 50000.00;
        double askPrice = 50000.50;

        // When: We create a new PriceTick
        // This line will NOT compile
        PriceTick tick = new PriceTick(pair, exchange, timestamp, bidPrice, askPrice);

        // Then: The getters should return the correct values
        assertEquals(pair, tick.getPair(), "CurrencyPair should be set");
        assertEquals(exchange, tick.getExchange(), "Exchange should be set");
        assertEquals(timestamp, tick.getTimestamp(), "Timestamp should be set");
        assertEquals(bidPrice, tick.getBidPrice(), "Bid price should be set");
        assertEquals(askPrice, tick.getAskPrice(), "Ask price should be set");
    }


}