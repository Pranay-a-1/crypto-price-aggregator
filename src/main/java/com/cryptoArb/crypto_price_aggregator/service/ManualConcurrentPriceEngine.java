package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Handles the mechanics of fetching prices concurrently using manual thread management.
 * <p>
 * <b>Educational Purpose:</b> Demonstrates usage of {@link ExecutorService} and {@link Future}
 * before moving to Spring's @Async or Reactive streams.
 * <p>
 * <b>Design Choice:</b> This class is a "worker" component. It doesn't know about business rules
 * (like aggregation), only about executing tasks in parallel.
 * <b>Phase 2 Refactor:</b> Added timeout handling to prevent indefinite blocking.
 * This is a critical "Defensive Coding" practice for concurrent systems.
 */
public class ManualConcurrentPriceEngine {

    private static final Logger log = LoggerFactory.getLogger(ManualConcurrentPriceEngine.class);

    // SLA: All fetches must complete within 3 seconds or be cancelled.
    // In a real app, this would be configurable via @Value
    private static final long FETCH_TIMEOUT_SECONDS = 5;

    private final ExecutorService executorService;

    /**
     * Creates an engine with a fixed thread pool.
     * * @param threadPoolSize Number of threads to maintain in the pool.
     */
    public ManualConcurrentPriceEngine(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        log.info("ManualConcurrentPriceEngine initialized with {} threads", threadPoolSize);
    }

    /**
     * Fetches prices from the provided fetchers concurrently.
     * * @param fetchers List of fetchers to query
     * @param pair The currency pair to fetch
     * @return List of successfully fetched PriceTicks (failed fetches are omitted)
     */
    public List<PriceTick> fetchPrices(List<PriceFetcher> fetchers, CurrencyPair pair) {
        if (fetchers == null || fetchers.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Create a list of Callables (tasks)
        List<Callable<PriceTick>> tasks = new ArrayList<>();
        for (PriceFetcher fetcher : fetchers) {
            tasks.add(() -> {
                log.debug("Submitting fetch task for {}", fetcher.getExchange());
                return fetcher.fetchPrice(pair);
            });
        }

        List<PriceTick> results = new ArrayList<>();

        try {
            // DEFENSIVE CODING: Use invokeAll with a timeout.
            // If the timeout expires, unfinished tasks are automatically cancelled.
            // This prevents the "Hanging Request" problem.
            // Hanging requests tie up server resources and degrade user experience.
            // In production, consider making the timeout configurable.
            // 'Hanging Request' means a request that takes an excessively long time to complete,
            // often due to waiting on slow external services, leading to poor user experience.
            //Q) what is a Future in java?
            //A) In Java, a Future represents the result of an asynchronous computation.
            // It acts as a placeholder for a value that will be available at some point in the future,
            // allowing you to check if the computation is complete, wait for its completion, and retrieve
            // the result once it's ready.
            // Future exists because it allows developers to write non-blocking code, where previous tasks can continue executing
            // while waiting for long-running operations to complete, thus improving application responsiveness and throughput.
            List<Future<PriceTick>> futures = executorService.invokeAll(
                    tasks, FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 3. Process results
            for (Future<PriceTick> future : futures) {
                try {
                    if (future.isCancelled()) {
                        log.warn("Parallel fetch task timed out after {}s", FETCH_TIMEOUT_SECONDS);
                        continue; // Skip cancelled tasks
                    }

                    PriceTick tick = future.get(); // Should return immediately if done , if not done throws exception because of timeout
                    if (tick != null) {
                        results.add(tick);
                    }
                } catch (ExecutionException e) {
                    // Task threw an exception (e.g., Network Error)
                    log.error("Error in parallel fetch: {}",
                            e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                } catch (CancellationException e) {
                    // Double-check for cancellation (invokeAll documentation guarantees this for timed-out tasks)
                    log.warn("Parallel fetch task was cancelled");
                }
            }
        } catch (InterruptedException e) {
            // Main thread was interrupted while waiting
            log.error("Engine execution interrupted", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }

        return results;
    }
}