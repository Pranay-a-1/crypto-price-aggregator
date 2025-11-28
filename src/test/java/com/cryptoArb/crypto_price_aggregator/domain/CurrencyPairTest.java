package com.cryptoArb.crypto_price_aggregator.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CurrencyPairTest {
    @Test
    void testBase() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        assertEquals("BTC", pair.base());
    }

    @Test
    void testQuote() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        System.out.println("pair: " + pair);
        assertEquals("USD", pair.quote());
    }

    // when base is null or blank
    @Test
    void testBaseNull() {
        assertThrows(IllegalArgumentException.class, () -> new CurrencyPair(null, "USD"));
    }

    // when quote is null or blank
    @Test
    void testQuoteNull() {
        assertThrows(IllegalArgumentException.class, () -> new CurrencyPair("BTC", null));
    }

}
