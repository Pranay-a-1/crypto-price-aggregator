package com.cryptoArb.domainOld_POJOs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceTickOldTest {

    @Test
    @DisplayName("Should create a PriceTick_POJO and verify all its properties")
    void givenTickData_whenCreatePriceTick_thenPropertiesAreSet() {
        // Given: All the data needed for a price tick
        CurrencyPair_POJO pair = new CurrencyPair_POJO("BTC", "USD");
        Exchange_POJO exchangeOld = new Exchange_POJO("coinbase");
        // --- This is the key change ---
        // We now want to use an Instant object directly, not a long
        Instant timestamp = Instant.now();
        // --- End of change ---
        // Use BigDecimal for prices
        BigDecimal bidPrice = new BigDecimal("50000.00");
        BigDecimal askPrice = new BigDecimal("50000.50");

        // When: We create a new PriceTick_POJO
        // This line will NOT compile
        PriceTick_POJO tick = new PriceTick_POJO(pair, exchangeOld, timestamp, bidPrice, askPrice);

        // Then: The getters should return the correct values
        assertEquals(pair, tick.getPair(), "CurrencyPair_POJO should be set");
        assertEquals(exchangeOld, tick.getExchange(), "Exchange_POJO should be set");
        assertEquals(timestamp, tick.getTimestamp(), "Timestamp should be set");
        assertEquals(bidPrice, tick.getBidPrice(), "Bid price should be set");
        assertEquals(askPrice, tick.getAskPrice(), "Ask price should be set");
    }


}