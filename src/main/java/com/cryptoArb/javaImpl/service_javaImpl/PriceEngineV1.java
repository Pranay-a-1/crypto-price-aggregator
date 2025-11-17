package com.cryptoArb.javaImpl.service_javaImpl;

import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.javaImpl.domain_records.PriceTick;
import com.cryptoArb.javaImpl.fetcher_javaImpl.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Phase 6: Classic Concurrency Engine (V1).
 * This service uses an ExecutorService to run all fetchers concurrently
 * and acts as a Producer, putting results into a BlockingQueue.
 *
 * It also runs a Consumer thread to process ticks from the queue.
 *
 *
 * PriceEngineV1 (Phase 6)
 * Architecture: Classic Producer-Consumer.
 *
 * Responsibility: Its job is very limited. It acted as two separate parts:
 *
 * Producer: Run fetchers and put raw PriceTicks into a BlockingQueue.
 *
 * Consumer: take raw PriceTicks from the queue and only save them to the database (databaseService.saveTick(tick)).
 *
 * It did not know how to aggregate prices or find arbitrage. It just saved the raw data. The assumption was that some other, separate part of the application would read from the database later to perform those tasks.
 */
public class PriceEngineV1 {

    // Logger for logging information and errors
    private static final Logger log = LoggerFactory.getLogger(PriceEngineV1.class);

    private final List<PriceFetcher> fetchers;
    private final BlockingQueue<PriceTick> tickQueue;
    private final DatabaseService databaseService;

    // --- Refactored Executors ---
    // A single-thread scheduler for the producers
    private final ScheduledExecutorService producerScheduler = Executors.newSingleThreadScheduledExecutor();
    // The pool for the fetchers to actually run on
    private final ExecutorService producerExecutor;
    // The single-thread consumer
    private final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor();

    // 'volatile' ensures visibility of this flag across threads
    private volatile boolean running = true; // Control flag for consumer thread


    /**
     * Constructor for the PriceEngine.
     *
     * @param fetchers        The list of PriceFetcher strategies to run.
     * @param tickQueue       The central queue to put results into.
     * @param databaseService The service to persist ticks.
     */
    public PriceEngineV1(List<PriceFetcher> fetchers,
                         BlockingQueue<PriceTick> tickQueue,
                         DatabaseService databaseService) { // Updated constructor
        this.fetchers = fetchers;
        this.tickQueue = tickQueue;
        this.databaseService = databaseService; // Assign new service
        // Create a fixed thread pool based on the number of fetchers
        this.producerExecutor = Executors.newFixedThreadPool(fetchers.size());
    }


    /**
     * Starts the entire engine.
     * 1. Starts the consumer thread.
     * 2. Schedules the producer (fetch cycle) to run every 5 seconds.
     */
    public void start() {
        log.info("Starting PriceEngineV1...");
        startConsumer(); // Start the consumer

        // Schedule the runFetchCycle task to run repeatedly
        producerScheduler.scheduleAtFixedRate(
                this::runFetchCycle, // The task to run // without method reference : () -> runFetchCycle(),
                0,                   // Initial delay (run immediately)
                5,                   // Period (run every 5)
                TimeUnit.SECONDS     // Time unit
        );
        log.info("Producer fetch cycle scheduled to run every 5 seconds.");
    }


    /**
     * Starts the consumer thread.
     * This method will return immediately, but the consumer
     * will be running in the background.
     * (Changed to private, as start() is now the public entry point)
     */
    void startConsumer() {
        log.info("Starting consumer thread...");
        consumerExecutor.submit(() -> { // Submit the consumer task
            while (running) {
                try {
                    // .take() blocks and waits until an item is available
                    PriceTick tick = tickQueue.take();

                    log.debug("Consumer took tick: {}", tick.exchange().id());

                    // This is the "processing" step
                    databaseService.saveTick(tick);

                } catch (InterruptedException e) {
                    // This is expected when we call stop()
                    log.info("Consumer thread interrupted. Shutting down.");
                    running = false; // Stop the loop
                    Thread.currentThread().interrupt(); // Restore interrupt status
                } catch (Exception e) {
                    // Catch any other exceptions (e.g., from saveTick)
                    log.error("Consumer error while processing tick: {}", e.getMessage(), e);
                }
            }
        });
    }



    /**
     * Runs one full fetch cycle, submitting each fetcher as a task to the thread pool.
     * (Changed to package-private or private, as it's now managed internally)
     */
     void runFetchCycle() { // Changed from public to package-private
        if (!running) {
            log.warn("Fetch cycle called, but engine is stopped.");
            return;
        }
        log.info("Starting new fetch cycle with {} fetchers...", fetchers.size());

        // Iterate over all fetchers and submit them to the executor
        for (PriceFetcher fetcher : fetchers) {

            // We submit a Runnable (a task) to the thread pool
            producerExecutor.submit(() -> { // This lambda is the task that runs on a separate thread
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

    }


    /**
     * Stops the engine, shutting down both producer and consumer executors.
     */
    public void stop() {
        log.info("Stopping PriceEngineV1...");
        this.running = false; // Signal consumer loop to stop

        // Shut down all three executors
        producerScheduler.shutdown();
        producerExecutor.shutdown();
        consumerExecutor.shutdownNow(); // Forcefully interrupt the consumer's .take()

        try {
            producerScheduler.awaitTermination(5, TimeUnit.SECONDS);
            producerExecutor.awaitTermination(5, TimeUnit.SECONDS);
            consumerExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for executors to shut down.");
            Thread.currentThread().interrupt();
        }
    }
}