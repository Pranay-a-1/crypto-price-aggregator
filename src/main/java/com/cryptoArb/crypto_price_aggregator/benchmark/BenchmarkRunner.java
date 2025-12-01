package com.cryptoArb.crypto_price_aggregator.benchmark;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.service.ManualConcurrentPriceEngine;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

/**
 * Utility component to run performance benchmarks comparing
 * Sequential execution vs. ManualConcurrentPriceEngine.
 * <p>
 * <b>Educational Purpose:</b> Demonstrates how to instrument and measure
 * concurrency improvements programmatically.
 * <b>Phase 2 Refactor:</b> Added statistical analysis (iterations, mean, stddev)
 * for more robust measurements.
 */
@Component
public class BenchmarkRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkRunner.class);
    private static final int DEFAULT_ITERATIONS = 5;

    /**
     * Executes the benchmark.
     *
     * @param fetchers List of fetchers to use (usually mocks with delays)
     * @param pair     Currency pair to fetch
     * @return Result containing timings and speedup factor
     */
    public BenchmarkResult runBenchmark(List<PriceFetcher> fetchers, CurrencyPair pair) {
        return runBenchmark(fetchers, pair, DEFAULT_ITERATIONS);
    }

    public BenchmarkResult runBenchmark(List<PriceFetcher> fetchers, CurrencyPair pair, int iterations) {
        log.info("Starting benchmark ({} iterations) with {} fetchers...", iterations, fetchers.size());

        List<Long> seqTimes = new ArrayList<>();
        List<Long> parTimes = new ArrayList<>();
        ManualConcurrentPriceEngine engine = new ManualConcurrentPriceEngine(fetchers.size());

        for (int i = 0; i < iterations; i++) {
            // Measure Sequential
            long startSeq = System.nanoTime();
            runSequential(fetchers, pair);
            seqTimes.add(System.nanoTime() - startSeq);

            // Measure Parallel
            long startPar = System.nanoTime();
            engine.fetchPrices(fetchers, pair);
            parTimes.add(System.nanoTime() - startPar);
        }

        // Calculate Stats
        Stats seqStats = calculateStats(seqTimes);
        Stats parStats = calculateStats(parTimes);
        double avgSpeedup = seqStats.mean / parStats.mean;

        log.info("Benchmark Completed:");
        log.info("Sequential: Mean={:.2f}ms, StdDev={:.2f}ms", seqStats.meanMs(), seqStats.stdDevMs());
        log.info("Parallel:   Mean={:.2f}ms, StdDev={:.2f}ms", parStats.meanMs(), parStats.stdDevMs());
        log.info("Average Speedup: {:.2f}x", avgSpeedup);

        return new BenchmarkResult(seqStats, parStats, avgSpeedup);
    }

    private void runSequential(List<PriceFetcher> fetchers, CurrencyPair pair) {
        for (PriceFetcher fetcher : fetchers) {
            try { fetcher.fetchPrice(pair); } catch (Exception ignored) {}
        }
    }

    private Stats calculateStats(List<Long> times) {
        LongSummaryStatistics summary = times.stream().mapToLong(Long::longValue).summaryStatistics();
        double mean = summary.getAverage();

        // Calculate Standard Deviation
        double variance = times.stream()
                .mapToDouble(t -> Math.pow(t - mean, 2))
                .sum() / times.size();
        double stdDev = Math.sqrt(variance);

        return new Stats(mean, stdDev, summary.getMin(), summary.getMax());
    }

    public record Stats(double mean, double stdDev, long min, long max) {
        public double meanMs() { return mean / 1_000_000.0; }
        public double stdDevMs() { return stdDev / 1_000_000.0; }
    }

    public record BenchmarkResult(Stats sequential, Stats parallel, double speedup) {}
}