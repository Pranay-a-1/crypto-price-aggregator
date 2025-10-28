package com.cryptoArb.domainOld;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExchangeOldTest {


    @Test
    @DisplayName("Should create an Exchange_old and verify its ID")
    void givenExchangeId_whenCreateExchange_thenIdIsSet() {
        // Given: An exchangeOld's unique identifier
        String exchangeId = "coinbase";

        // When: We create a new Exchange_old object
        // This line will NOT compile
        Exchange_old exchangeOld = new Exchange_old(exchangeId);

        // Then: The getId() method should return the correct ID
        assertEquals(exchangeId, exchangeOld.getId(), "The exchangeOld ID should be set correctly");
    }

}