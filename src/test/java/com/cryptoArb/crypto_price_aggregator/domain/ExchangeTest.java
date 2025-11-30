package com.cryptoArb.crypto_price_aggregator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeTest {

    @Test
    @DisplayName("All exchanges should have non-empty display names")
    void allExchangesShouldHaveDisplayNames() {
        for (Exchange exchange : Exchange.values()) {
            assertNotNull(exchange.getDisplayName(),
                    "Exchange " + exchange + " should have a display name");
            assertFalse(exchange.getDisplayName().isBlank(),
                    "Exchange " + exchange + " display name should not be blank");
        }
    }

    @Test
    @DisplayName("Should be able to get exchange by name")
    void shouldBeAbleToGetExchangeByName() {
        Exchange binance = Exchange.valueOf("BINANCE");
        assertEquals(Exchange.BINANCE, binance);
        assertEquals("Binance", binance.getDisplayName());
    }

    @Test
    @DisplayName("Should throw exception for invalid exchange name")
    void shouldThrowExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Exchange.valueOf("INVALID_EXCHANGE");
        });
        // without lambda to do the same of assertThrows
        try {
            Exchange.valueOf("INVALID_EXCHANGE");
            fail("Should throw exception for invalid exchange name");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
