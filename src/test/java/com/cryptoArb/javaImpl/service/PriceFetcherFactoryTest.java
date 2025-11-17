package com.cryptoArb.javaImpl.service;

import com.cryptoArb.exception.InvalidPairException;
import com.cryptoArb.fetcher.BinanceFetcher;
import com.cryptoArb.fetcher.CoinbaseFetcher;
import com.cryptoArb.fetcher.PriceFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriceFetcherFactoryTest {

    private PriceFetcherFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PriceFetcherFactory();
    }

    @Test
    @DisplayName("Should return CoinbaseFetcher for 'coinbase' ID")
    void shouldReturnCoinbaseFetcher() {
        // Given
        String exchangeId = "coinbase";

        // When
        PriceFetcher fetcher = factory.createFetcher(exchangeId);

        // Then
        assertNotNull(fetcher);
        assertInstanceOf(CoinbaseFetcher.class, fetcher, "Factory should return an instance of CoinbaseFetcher");
    }

    @Test
    @DisplayName("Should return BinanceFetcher for 'binance' ID")
    void shouldReturnBinanceFetcher() {
        // Given
        String exchangeId = "binance";

        // When
        PriceFetcher fetcher = factory.createFetcher(exchangeId);

        // Then
        assertNotNull(fetcher);
        assertTrue(fetcher instanceof BinanceFetcher, "Factory should return an instance of BinanceFetcher");
    }


    @Test
    @DisplayName("Should throw InvalidPairException for unknown ID")
    void shouldThrowForUnknownId() {
        // Given
        String exchangeId = "unknown-exchange";

        // When & Then
        // Assert that executing the createFetcher method throws our exception
        Exception exception = assertThrows(InvalidPairException.class, () -> {
            factory.createFetcher(exchangeId);
        });

        // We can also check that the message is helpful
        String expectedMessage = "No fetcher available for exchange: " + exchangeId;
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}