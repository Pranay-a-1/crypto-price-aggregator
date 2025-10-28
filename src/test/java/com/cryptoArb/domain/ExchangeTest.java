package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExchangeTest {

    @Test
    @DisplayName("Should create an Exchange_old and verify its ID")
    void givenExchangeId_whenCreateExchange_thenIdIsSet() {
        // Given: An exchange's unique identifier
        String exchangeId = "coinbase";

        // When: We create a new Exchange_old object
        // This line will NOT compile
        Exchange exchange = new Exchange(exchangeId);

        // Then: The getId() method should return the correct ID
        assertEquals(exchangeId, exchange.id(), "The exchange ID should be set correctly");
    }

}