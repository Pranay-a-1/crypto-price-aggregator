package com.cryptoArb.javaImpl.domain_POJOs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyPairOldTest {

    @Test
    @DisplayName("Should correctly create a CurrencyPair_POJO and verify its properties")
    void givenBaseAndQuote_whenCreateCurrencyPair_thenPropertiesAreSet() {
        // Given: We have a base currency and a quote currency
        String baseCurrency = "BTC";
        String quoteCurrency = "USD";

        // When: We create a new CurrencyPair_POJO
        // This line will NOT compile, which is what we want!
        CurrencyPair_POJO pair = new CurrencyPair_POJO(baseCurrency, quoteCurrency);

        // Then: The getters should return the correct values
        assertEquals(baseCurrency, pair.getBase(), "The base currency should be BTC");
        assertEquals(quoteCurrency, pair.getQuote(), "The quote currency should be USD");
    }


    @Test
    @DisplayName("Should correctly test for equality between two CurrencyPair_POJO objects")
    void givenTwoEqualCurrencyPairs_whenCheckEquals_thenReturnsTrue() {
        // Given: Two separate instances with the same values
        CurrencyPair_POJO pair1 = new CurrencyPair_POJO("BTC", "USD");
        CurrencyPair_POJO pair2 = new CurrencyPair_POJO("BTC", "USD");

        // When: We check for equality
        boolean areEqual = pair1.equals(pair2);

        // Then: They should be considered equal
        assertTrue(areEqual, "Two pairs with the same base and quote should be equal");
    }

    @Test
    @DisplayName("Should correctly test for inequality")
    void givenTwoDifferentCurrencyPairs_whenCheckEquals_thenReturnsFalse() {
        // Given: Two different pairs
        CurrencyPair_POJO pair1 = new CurrencyPair_POJO("BTC", "USD");
        CurrencyPair_POJO pair2 = new CurrencyPair_POJO("ETH", "USD");
        CurrencyPair_POJO pair3 = new CurrencyPair_POJO("BTC", "EUR");

        // When / Then
        assertFalse(pair1.equals(pair2), "Should be false for different base currency");
        assertFalse(pair1.equals(pair3), "Should be false for different quote currency");
        assertFalse(pair1.equals(null), "Should be false when compared to null");
        assertFalse(pair1.equals(new Object()), "Should be false when compared to different object type");


    }

}