package com.cryptoArb.service;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PriceEngineV1Test {

    private PriceFetcher mockFetcher1;
    private PriceFetcher mockFetcher2;
    private List<PriceFetcher> fetchers;
    private BlockingQueue<PriceTick> tickQueue;
    private PriceEngineV1 priceEngine; // This class doesn't exist yet

    // Helper to create dummy ticks
    private PriceTick tick1 = new PriceTick(new CurrencyPair("BTC", "USD"), new Exchange("mock1"), Instant.now(), BigDecimal.ONE, BigDecimal.TEN);
    private PriceTick tick2 = new PriceTick(new CurrencyPair("ETH", "USD"), new Exchange("mock2"), Instant.now(), BigDecimal.ONE, BigDecimal.TEN);

    @BeforeEach
    void setUp() throws PriceFetchException {
        // 1. Given: A queue and a list of mock fetchers
        tickQueue = new LinkedBlockingQueue<>();

        // Mock fetcher 1
        // this mocks the PriceFetcher interface, like CoinbaseFetcher or BinanceFetcher
        // so that we can control its behavior in the test
        // when fetchPrices() is called, it will return a list with tick1
        mockFetcher1 = Mockito.mock(PriceFetcher.class);
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1));

        // Mock fetcher 2
        mockFetcher2 = Mockito.mock(PriceFetcher.class);
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2));

        fetchers = List.of(mockFetcher1, mockFetcher2);

        // 2. When: We create the engine
        priceEngine = new PriceEngineV1(fetchers, tickQueue);
    }

    @Test
    @DisplayName("Should run all fetchers concurrently and add all ticks to the queue")
    void shouldRunFetchersAndFillQueue() throws Exception {
        // 3. When: We run one fetch cycle
        priceEngine.runFetchCycle();

        // 4. Then: The queue should contain all ticks from all fetchers

        // We need to wait for the concurrent tasks to finish.
        // For a simple test, we can just sleep briefly.
        // A more robust test would use CountDownLatch, but let's start simple.
        TimeUnit.SECONDS.sleep(1);

        assertEquals(2, tickQueue.size(), "Queue should have two ticks");
        assertTrue(tickQueue.contains(tick1), "Queue should contain tick from fetcher 1");
        assertTrue(tickQueue.contains(tick2), "Queue should contain tick from fetcher 2");
    }
}