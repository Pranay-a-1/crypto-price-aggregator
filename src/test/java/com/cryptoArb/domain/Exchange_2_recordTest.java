package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Exchange_2_recordTest {

    @Test
    @DisplayName("Should create an Exchange and verify its ID")
    void givenExchangeId_whenCreateExchange_thenIdIsSet() {
        // Given: An exchange's unique identifier
        String exchangeId = "coinbase";

        // When: We create a new Exchange object
        // This line will NOT compile
        Exchange_2_record exchange = new Exchange_2_record(exchangeId);

        // Then: The getId() method should return the correct ID
        assertEquals(exchangeId, exchange.Id(), "The exchange ID should be set correctly");
    }

}