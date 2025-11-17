package com.cryptoArb.javaImpl.domain_POJOs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExchangeOldTest {


    @Test
    @DisplayName("Should create an Exchange_POJO and verify its ID")
    void givenExchangeId_whenCreateExchange_thenIdIsSet() {
        // Given: An exchangeOld's unique identifier
        String exchangeId = "coinbase";

        // When: We create a new Exchange_POJO object
        // This line will NOT compile
        Exchange_POJO exchangeOld = new Exchange_POJO(exchangeId);

        // Then: The getId() method should return the correct ID
        assertEquals(exchangeId, exchangeOld.getId(), "The exchangeOld ID should be set correctly");
    }

}