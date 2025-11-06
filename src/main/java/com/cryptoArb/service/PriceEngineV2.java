package com.cryptoArb.service;

import com.cryptoArb.fetcher.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Phase 8: Modern Concurrency Engine (V2).
 * This service uses CompletableFuture to run all fetchers asynchronously
 * and build a non-blocking data pipeline.
 *
 * PriceEngineV2 (Phase 8)
 * Architecture: Modern Asynchronous Pipeline (using CompletableFuture).
 *
 * Responsibility: Its job is to be the central orchestrator for the entire data pipeline, from start to finish, all in one non-blocking flow.
 *
 * According to the Phase 8 plan, PriceEngineV2 is responsible for:
 *
 * Fetching all the raw PriceTicks (needs PriceFetchers).
 *
 * Saving those raw ticks (needs DatabaseService).
 *
 * Aggregating the ticks into a ConsolidatedPrice map (needs PriceService).
 *
 * Analyzing that map for arbitrage (needs ArbitrageService).
 *
 * Collecting any found opportunities (needs OpportunityAggregator).
 *
 *
 */
public class PriceEngineV2 {

    private static final Logger log = LoggerFactory.getLogger(PriceEngineV2.class);

    // All the services needed for our async pipeline
    private final List<PriceFetcher> fetchers;
    private final DatabaseService databaseService;
    private final PriceService priceService;
    private final ArbitrageService arbitrageService;
    private final OpportunityAggregator opportunityAggregator;

    // The thread pool to run our async tasks
    private final ExecutorService executor;

    public PriceEngineV2(List<PriceFetcher> fetchers,
                         DatabaseService databaseService,
                         PriceService priceService,
                         ArbitrageService arbitrageService,
                         OpportunityAggregator opportunityAggregator) {
        this.fetchers = fetchers;
        this.databaseService = databaseService;
        this.priceService = priceService;
        this.arbitrageService = arbitrageService;
        this.opportunityAggregator = opportunityAggregator;
        // We create a thread pool sized to our number of fetchers
        this.executor = Executors.newFixedThreadPool(fetchers.size());
    }

    /**
     * Runs one full asynchronous fetch-and-process cycle.
     */
    public CompletableFuture<Void> runFetchCycle() {
        // This is the method we will implement and test
        log.info("Starting new async fetch cycle...");

        // For now, it does nothing and just returns a completed future
        return CompletableFuture.completedFuture(null);
    }


    /**
     * Stops the engine by shutting down its internal thread pool.
     */
    public void stop() {
        log.info("Stopping PriceEngineV2...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in time. Forcing shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for executor to shut down.");
            Thread.currentThread().interrupt();
        }
    }
}
