package com.cryptoArb.fetcher;

import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.javaImpl.domain_records.PriceTick;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceFetcherContractTest {

    // Test 1: Test the exception contract
    // This inner class is a "Test-Specific Subclass".
    // It's a mock object we create just for this test.
    class FailingFetcher implements PriceFetcher {
        @Override
        public List<PriceTick> fetchPrices() throws PriceFetchException {
            // This fetcher's only job is to fail
            throw new PriceFetchException("Simulated network error");
        }
    }

    @Test
    @DisplayName("Fetcher should throw PriceFetchException on failure")
    void fetcherShouldThrowOnFailure() {
        // Given
        // This line is now GREEN
        PriceFetcher failingFetcher = new FailingFetcher();

        // When & Then
        Exception exception = assertThrows(PriceFetchException.class, () -> {
            failingFetcher.fetchPrices();
        });

        assertEquals("Simulated network error", exception.getMessage());
    }

    // Test 2: Test the default method
    @Test
    @DisplayName("Default method should return correct exchange name")
    void defaultMethodShouldReturnExchangeName() {
        // Given
        PriceFetcher coinbase = new CoinbaseFetcher();

        // When
        // This line is now GREEN
        String exchangeName = coinbase.getExchangeName();

        // Then
        assertEquals("Coinbase", exchangeName);
    }
}