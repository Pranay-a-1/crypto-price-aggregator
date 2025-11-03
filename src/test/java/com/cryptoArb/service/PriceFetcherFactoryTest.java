package com.cryptoArb.service;

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
}