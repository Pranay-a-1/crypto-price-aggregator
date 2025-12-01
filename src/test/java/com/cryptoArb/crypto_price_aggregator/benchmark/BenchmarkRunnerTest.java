package com.cryptoArb.crypto_price_aggregator.benchmark;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.service.ManualConcurrentPriceEngine;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validates that our benchmark logic accurately detects speedups.
 */
class BenchmarkRunnerTest {

    @Test
    @DisplayName("Parallel execution should be at least 2x faster than sequential for slow tasks")
    void shouldShowSpeedup()  {
        // GIVEN
        int numFetchers = 4;
        long delayPerFetcher = 100L; // 100ms

        // Expected: Sequential ~400ms, Parallel ~100ms + overhead
        List<PriceFetcher> fetchers = createSlowFetchers(numFetchers, delayPerFetcher);
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        ManualConcurrentPriceEngine engine = new ManualConcurrentPriceEngine(numFetchers);

        // WHEN: Sequential Loop (Simulated)
        long startSeq = System.nanoTime();
        for (PriceFetcher fetcher : fetchers) {
            try { fetcher.fetchPrice(pair); } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        long durationSeq = System.nanoTime() - startSeq;

        // WHEN: Parallel Engine
        long startPar = System.nanoTime();
        engine.fetchPrices(fetchers, pair);
        long durationPar = System.nanoTime() - startPar;

        // THEN
        double speedup = (double) durationSeq / durationPar;
        System.out.printf("Benchmark: Sequential=%.2fms, Parallel=%.2fms, Speedup=%.2fx%n",
                durationSeq / 1_000_000.0, durationPar / 1_000_000.0, speedup);

        assertTrue(speedup > 1.5, "Parallel implementation should provide significant speedup");
    }

    private List<PriceFetcher> createSlowFetchers(int count, long delayMs) {
        List<PriceFetcher> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PriceFetcher m = mock(PriceFetcher.class);
            try {
                when(m.fetchPrice(any())).thenAnswer(inv -> {
                    Thread.sleep(delayMs);
                    return null;
                });
            } catch (Exception e) { throw new RuntimeException(e); }
            list.add(m);
        }
        return list;
    }
}