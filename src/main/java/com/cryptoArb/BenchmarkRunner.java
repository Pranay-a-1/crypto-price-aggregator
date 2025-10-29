package com.cryptoArb;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.service.PriceService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A simple benchmark to compare sequential vs. parallel stream performance
 * for our aggregation logic, as required by Phase 2.
 */
public class BenchmarkRunner {

    // --- Configuration ---
    private static final int TICK_COUNT = 1_000_000; // 1 Million ticks
    private static final PriceService priceService = new PriceService();

    // --- Mock Data ---
    private static final List<Exchange> EXCHANGES = List.of(
            new Exchange("coinbase"), new Exchange("kraken"),
            new Exchange("binance"), new Exchange("bitfinex")
    );
    private static final List<CurrencyPair> PAIRS = List.of(
            new CurrencyPair("BTC", "USD"), new CurrencyPair("ETH", "USD"),
            new CurrencyPair("BTC", "EUR"), new CurrencyPair("LTC", "USD")
    );
    private static final Random random = new Random();

    /**
     * Generates a large list of random price ticks.
     */
    private static List<PriceTick> generateMockTicks() {
        System.out.println("Generating " + TICK_COUNT + " mock ticks...");
        List<PriceTick> ticks = new ArrayList<>(TICK_COUNT);
        for (int i = 0; i < TICK_COUNT; i++) {
            ticks.add(createRandomTick());
        }
        System.out.println("Generation complete.");
        return ticks;
    }

    /**
     * Helper to create a single random tick.
     */
    private static PriceTick createRandomTick() {
        CurrencyPair pair = PAIRS.get(random.nextInt(PAIRS.size()));
        Exchange exchange = EXCHANGES.get(random.nextInt(EXCHANGES.size()));
        Instant timestamp = Instant.now().minusSeconds(random.nextInt(3600)); // Within last hour

        // Generate a random price (e.g., 50000 +/- 100)
        BigDecimal basePrice = new BigDecimal("50000");
        BigDecimal priceFluctuation = new BigDecimal(random.nextDouble() * 100); // 0 to 100
        BigDecimal bidPrice = basePrice.add(priceFluctuation); // example bid price could be 50050.23
        BigDecimal askPrice = bidPrice.add(new BigDecimal("0.50")); // Ask is always slightly higher

        return new PriceTick(pair, exchange, timestamp, bidPrice, askPrice);
    }

    /**
     * Main entry point to run the benchmark.
     */
    public static void main(String[] args) {
        List<PriceTick> ticks = generateMockTicks();

        // --- 1. Run Sequential Benchmark ---
        System.out.println("\nRunning SEQUENTIAL aggregation...");
        long startTimeSequential = System.nanoTime();

        // Call the sequential method
        priceService.aggregatePrices(ticks);

        long endTimeSequential = System.nanoTime();
        long durationSequential = TimeUnit.NANOSECONDS.toMillis(endTimeSequential - startTimeSequential);
        System.out.println("SEQUENTIAL aggregation took: " + durationSequential + " ms");


        // --- 2. Run Parallel Benchmark ---
        System.out.println("\nRunning PARALLEL aggregation...");
        long startTimeParallel = System.nanoTime();

        // Call the parallel method
        priceService.aggregatePricesParallel(ticks);

        long endTimeParallel = System.nanoTime();
        long durationParallel = TimeUnit.NANOSECONDS.toMillis(endTimeParallel - startTimeParallel);
        System.out.println("PARALLEL aggregation took: " + durationParallel + " ms");


        // --- 3. Log Results ---
        System.out.println("\n--- Benchmark Complete ---");
        System.out.println("Sequential: " + durationSequential + " ms");
        System.out.println("Parallel:   " + durationParallel + " ms");
    }
}