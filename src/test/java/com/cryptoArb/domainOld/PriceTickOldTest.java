package com.cryptoArb.domainOld;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceTickOldTest {

    @Test
    @DisplayName("Should create a PriceTick_old and verify all its properties")
    void givenTickData_whenCreatePriceTick_thenPropertiesAreSet() {
        // Given: All the data needed for a price tick
        CurrencyPair_old pair = new CurrencyPair_old("BTC", "USD");
        Exchange_old exchangeOld = new Exchange_old("coinbase");
        long timestamp = Instant.now().toEpochMilli();
        // Use BigDecimal for prices
        BigDecimal bidPrice = new BigDecimal("50000.00");
        BigDecimal askPrice = new BigDecimal("50000.50");

        // When: We create a new PriceTick_old
        // This line will NOT compile
        PriceTick_old tick = new PriceTick_old(pair, exchangeOld, timestamp, bidPrice, askPrice);

        // Then: The getters should return the correct values
        assertEquals(pair, tick.getPair(), "CurrencyPair_old should be set");
        assertEquals(exchangeOld, tick.getExchange(), "Exchange_old should be set");
        assertEquals(timestamp, tick.getTimestamp(), "Timestamp should be set");
        assertEquals(bidPrice, tick.getBidPrice(), "Bid price should be set");
        assertEquals(askPrice, tick.getAskPrice(), "Ask price should be set");
    }


}