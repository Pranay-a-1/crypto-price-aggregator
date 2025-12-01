package com.cryptoArb.crypto_price_aggregator.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CurrencyPairTest {
    @Test
    void testBase() {
        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        assertEquals("BTC", pair.getBase());
    }

    @Test
    void testQuote() {
        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        System.out.println("pair: " + pair);
        assertEquals("USD", pair.getQuote());
    }

    // when base is null or blank
    @Test
    void testBaseNull() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyPair.of(null, "USD"));
    }

    // when quote is null or blank
    @Test
    void testQuoteNull() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyPair.of("BTC", null));
    }

}
