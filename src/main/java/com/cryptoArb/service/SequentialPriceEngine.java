package com.cryptoArb.service;


import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A simple, sequential engine for fetching and processing prices.
 * This engine processes each fetcher one by one.
 *
 * Architecture: Sequential (Blocking)
 * Responsibility: Orchestrates the fetch-and-save cycle in a single-threaded,
 * easy-to-read manner.
 *
 * This is a blocking implementation, where each fetcher is processed one after the other.
 * It's simple and easy to understand, but not the most efficient.
 *
 * It is a blocking implementation of the PriceEngineV2 class
 */
public class SequentialPriceEngine {

    private static final Logger log = LoggerFactory.getLogger(SequentialPriceEngine.class);

    private final List<PriceFetcher> fetchers;
    private final DatabaseService databaseService;

    public SequentialPriceEngine(List<PriceFetcher> fetchers,
                                 DatabaseService databaseService) {
        this.fetchers = fetchers;
        this.databaseService = databaseService;
    }

    /**
     * Runs one full sequential fetch-and-process cycle.
     * It iterates through each fetcher, attempts to fetch prices,
     * and saves them to the database.
     *
     * If a fetcher fails with a PriceFetchException, it logs the error
     * and continues to the next fetcher, ensuring the engine is resilient.
     */
    public void runFetchCycle() {
        log.info("Starting new sequential fetch cycle...");

        // === NEW CODE START ===

        // 1. Iterate through each fetcher in our list
        for (PriceFetcher fetcher : fetchers) {
            // The try-catch block is *inside* the loop.
            // This is a key design choice for resiliency.
            // Q) How?
            // A) If a fetcher fails, it logs the error and continues to the next fetcher.
            try {
                log.debug("Fetching from {}", fetcher.getExchangeName());

                // 2. Call the fetcher's fetchPrices() method
                // In our test, this is where the mock returns its programmed list
                // This is the "risky" line that can throw
                List<PriceTick> ticks = fetcher.fetchPrices();

                // 3. Iterate through the list of ticks we just received
                // This code only runs if fetchPrices() succeeds
                for (PriceTick tick : ticks) {
                    log.debug("Saving tick: {}", tick);

                    // 4. Call the database service for each tick
                    // In our test, this is what Mockito is counting
                    databaseService.saveTick(tick);
                }
            } catch (PriceFetchException e) {
                // If a fetcher fails, we log the error and the loop continues
                // to the next fetcher. This makes our engine robust.
                log.error("Failed to fetch prices from {}: {}",
                        fetcher.getExchangeName(), e.getMessage());
            }
        }

        // === NEW CODE END ===

        log.info("Sequential fetch cycle finished.");
    }
}