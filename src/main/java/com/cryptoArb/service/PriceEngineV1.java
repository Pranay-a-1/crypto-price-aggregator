package com.cryptoArb.service;

import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Phase 6: Classic Concurrency Engine (V1).
 * This service uses an ExecutorService to run all fetchers concurrently
 * and acts as a Producer, putting results into a BlockingQueue.
 */
public class PriceEngineV1 {

    // Logger for logging information and errors
    private static final Logger log = LoggerFactory.getLogger(PriceEngineV1.class);

    private final List<PriceFetcher> fetchers;
    private final BlockingQueue<PriceTick> tickQueue;
    private final ExecutorService executor;

    /**
     * Constructor for the PriceEngine.
     *
     * @param fetchers  The list of PriceFetcher strategies to run.
     * @param tickQueue The central queue to put results into.
     */
    public PriceEngineV1(List<PriceFetcher> fetchers, BlockingQueue<PriceTick> tickQueue) {
        this.fetchers = fetchers;
        this.tickQueue = tickQueue;
        // Create a fixed thread pool based on the number of fetchers [cite: 92]
        this.executor = Executors.newFixedThreadPool(fetchers.size());
    }

    /**
     * Runs one full fetch cycle, submitting each fetcher as a task
     * to the thread pool.
     */
    public void runFetchCycle() {
        log.info("Starting new fetch cycle with {} fetchers...", fetchers.size());

        // Iterate over all fetchers and submit them to the executor
        for (PriceFetcher fetcher : fetchers) {

            // We submit a Runnable (a task) to the thread pool
            executor.submit(() -> { // This lambda is the task that runs on a separate thread
                try {
                    // 1. Fetch prices (this is the I/O call)
                    List<PriceTick> ticks = fetcher.fetchPrices();

                    // 2. Add all results to the queue (Producer action)
                    log.debug("Fetcher {} found {} ticks", fetcher.getExchangeName(), ticks.size());
                    for (PriceTick tick : ticks) {
                        // .put() will wait if the queue is full
                        tickQueue.put(tick);
                    }

                } catch (PriceFetchException e) {
                    log.error("Failed to fetch prices from {}: {}", fetcher.getExchangeName(), e.getMessage());
                } catch (InterruptedException e) {
                    log.warn("Producer thread for {} interrupted while putting to queue", fetcher.getExchangeName());
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Note: We don't call executor.shutdown() here, as this engine
        // is intended to run multiple cycles.
        // because we want to keep reusing the thread pool.
        /**
         * I didn't call executor.shutdown(), and that was intentional.
         *
         * Here’s why:
         *
         * It's a Long-Running Service: The PriceEngineV1 isn't a one-time script. According to the project plan, the goal is to use a ScheduledExecutorService to run this fetch cycle repeatedly (e.g., every 5 seconds).
         *
         * What shutdown() Does: Calling executor.shutdown() tells the thread pool: "Don't accept any new tasks, and shut down completely once all current tasks are finished."
         *
         * The Problem: If we called executor.shutdown() at the end of runFetchCycle(), the first cycle would work perfectly. But when the ScheduledExecutorService tried to run the cycle again 5 seconds later, the executor would be dead. Submitting new tasks would fail, throwing a RejectedExecutionException.
         *
         * In short, the ExecutorService is a permanent component of our PriceEngineV1 service. It needs to stay alive to process all future fetch cycles for as long as the application is running.
         */

    }
}