package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ManualConcurrentPriceEngine.
 * Focus: Verifying parallel execution and resource management.
 */
class ManualConcurrentPriceEngineTest {

    private final CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");

    @Test
    @DisplayName("Should fetch prices from all fetchers concurrently (Duration < Sum of delays)")
    @Timeout(value = 2, unit = TimeUnit.SECONDS) // Safety net
    void shouldFetchPricesInParallel() {
        // GIVEN: 3 fetchers that each take 500ms to respond
        // If sequential: 500ms * 3 = 1500ms
        // If parallel: max(500ms) + overhead ~= 500-600ms

        long delayMs = 500;
        PriceFetcher fetcher1 = createSlowMockFetcher(delayMs, Exchange.BINANCE);
        PriceFetcher fetcher2 = createSlowMockFetcher(delayMs, Exchange.COINBASE);
        PriceFetcher fetcher3 = createSlowMockFetcher(delayMs, Exchange.KRAKEN);

        List<PriceFetcher> fetchers = List.of(fetcher1, fetcher2, fetcher3);

        // Use a fixed thread pool size > number of fetchers to ensure full parallelism
        ManualConcurrentPriceEngine engine = new ManualConcurrentPriceEngine(4);

        // WHEN: Fetching prices
        long start = System.nanoTime();
        List<PriceTick> results = engine.fetchPrices(fetchers, btcUsd);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // THEN:
        // 1. Verify correctness
        assertEquals(3, results.size(), "Should return results from all 3 fetchers");

        // 2. Verify parallelism
        // Allow some overhead (e.g., 500ms delay + 400ms overhead = 900ms)
        // Must be significantly less than sequential time (1500ms)
        assertTrue(durationMs < (delayMs * 2),
                String.format("Execution took %d ms, expected < %d ms (Parallel execution)", durationMs, delayMs * 2));
    }

    @Test
    @DisplayName("Should collect successful results even if some fetchers fail")
    void shouldHandlePartialFailures() throws PriceFetchException {
        // GIVEN: 1 successful fetcher, 1 failing fetcher
        PriceFetcher successFetcher = mock(PriceFetcher.class);
        when(successFetcher.fetchPrice(any())).thenReturn(
                new PriceTick(btcUsd, Exchange.BINANCE, BigDecimal.TEN, BigDecimal.TEN, Instant.now())
        );

        PriceFetcher failFetcher = mock(PriceFetcher.class);
        when(failFetcher.fetchPrice(any())).thenThrow(new PriceFetchException("Simulated API Error"));

        ManualConcurrentPriceEngine engine = new ManualConcurrentPriceEngine(2);

        // WHEN
        List<PriceTick> results = engine.fetchPrices(List.of(successFetcher, failFetcher), btcUsd);

        // THEN
        assertEquals(1, results.size(), "Should return only the successful tick");
        assertEquals(Exchange.BINANCE, results.get(0).getExchange());
    }

    // Helper to create a mock that sleeps before returning
    private PriceFetcher createSlowMockFetcher(long delayMs, Exchange exchange) {
        PriceFetcher fetcher = mock(PriceFetcher.class);
        try {
            // We only catch PriceFetchException here because fetchPrice declares it.
            when(fetcher.fetchPrice(any())).thenAnswer(invocation -> {
                try {
                    // Thread.sleep throws InterruptedException, which must be handled INSIDE the lambda
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Mock interrupted", e);
                }
                return new PriceTick(btcUsd, exchange, BigDecimal.TEN, BigDecimal.TEN, Instant.now());
            });
        } catch (PriceFetchException e) {
            // This catch block is required because fetchPrice is a checked exception
            throw new RuntimeException(e);
        }
        return fetcher;
    }
}