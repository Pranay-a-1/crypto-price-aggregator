package com.cryptoArb.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyPair_2_recordTest {


    @Test
    @DisplayName("Should correctly create a CurrencyPair and verify its properties")
    void givenBaseAndQuote_whenCreateCurrencyPair_thenPropertiesAreSet() {
        // Given
        String baseCurrency = "BTC";
        String quoteCurrency = "USD";

        // When
        CurrencyPair_2_record pair = new CurrencyPair_2_record(baseCurrency, quoteCurrency);

        // Then: Use the new accessor methods
        assertEquals(baseCurrency, pair.base(), "The base currency should be BTC");
        assertEquals(quoteCurrency, pair.quote(), "The quote currency should be USD");
    }



    @Test
    @DisplayName("Should correctly test for equality between two CurrencyPair objects")
    void givenTwoEqualCurrencyPairs_whenCheckEquals_thenReturnsTrue() {
        // Given
        CurrencyPair_2_record pair1 = new CurrencyPair_2_record("BTC", "USD");
        CurrencyPair_2_record pair2 = new CurrencyPair_2_record("BTC", "USD");

        // When / Then
        // NO CHANGE NEEDED! The record's .equals() works perfectly.
        assertTrue(pair1.equals(pair2), "Two pairs with the same base and quote should be equal");
        assertEquals(pair1.hashCode(), pair2.hashCode(), "Hashcodes should also be equal");
    }

    @Test
    @DisplayName("Should correctly test for inequality")
    void givenTwoDifferentCurrencyPairs_whenCheckEquals_thenReturnsFalse() {
        // ... (Given)
        CurrencyPair_2_record pair1 = new CurrencyPair_2_record("BTC", "USD");
        CurrencyPair_2_record pair2 = new CurrencyPair_2_record("ETH", "USD");

        // When / Then
        // NO CHANGE NEEDED! This test still passes.
        assertFalse(pair1.equals(pair2), "Should be false for different base currency");
    }

    // Let's add a test to prove the auto-generated toString() works

    @Test
    @DisplayName("Should provide a useful toString() representation")
    void givenPair_whenCallToString_thenReturnsCorrectFormat() {
        // Given
        CurrencyPair_2_record pair = new CurrencyPair_2_record("BTC", "USD");

        // When
        String expected = "CurrencyPair_2_record[base='BTC', quote='USD']";

        // Then
        assertEquals(expected, pair.toString());
    }

}