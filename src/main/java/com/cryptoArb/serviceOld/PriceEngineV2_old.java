package com.cryptoArb.serviceOld;

import com.cryptoArb.domain.ArbitrageOpportunity;
import com.cryptoArb.domain.ConsolidatedPrice;
import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.DatabaseService;
import com.cryptoArb.service.OpportunityAggregator;
import com.cryptoArb.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class PriceEngineV2_old {

    private static final Logger log = LoggerFactory.getLogger(PriceEngineV2_old.class);

    // All the services needed for our async pipeline
    private final List<PriceFetcher> fetchers;
    private final DatabaseService databaseService;
    private final PriceService priceService;
    private final ArbitrageService arbitrageService;
    private final OpportunityAggregator opportunityAggregator;

    // The thread pool to run our async tasks
    private final ExecutorService executor;

    public PriceEngineV2_old(List<PriceFetcher> fetchers,
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
        // Ensure at least 1 thread even if fetchers list is empty
        this.executor = Executors.newFixedThreadPool(Math.max(fetchers.size(), 1));
    }

    /**
     * Runs one full asynchronous fetch-and-process cycle.
     */
    public CompletableFuture<Void> runFetchCycle() {
        log.info("Starting new async fetch cycle...");

        // 1. Create a list of async tasks to fetch prices
        // We get a List<CompletableFuture<List<PriceTick>>>
        List<CompletableFuture<List<PriceTick>>> fetchFutures = fetchers.stream()
                .map(fetcher -> CompletableFuture.supplyAsync(() -> {
                                    try {
                                        log.debug("Fetching from {}", fetcher.getExchangeName());
                                        // This is the blocking I/O call
                                        return fetcher.fetchPrices();
                                    } catch (PriceFetchException e) {
                                        log.error("Failed to fetch prices from {}: {}", fetcher.getExchangeName(), e.getMessage());
                                        // As per plan, handle failure gracefully
                                        return List.<PriceTick>of(); // Return an empty list
                                    }
                                }, executor)
                                // This .exceptionally() is a safeguard for other RuntimeExceptions
                                .exceptionally(ex -> {
                                    log.error("Unhandled exception in fetcher {}: {}", fetcher.getExchangeName(), ex.getMessage());
                                    return List.<PriceTick>of();
                                })
                )
                .collect(Collectors.toList());

        // 2. Create a "master" future that completes when ALL fetches are done.
        // We need to collect all the individual lists into one big list.
        CompletableFuture<Void> allOfFetchers = CompletableFuture.allOf(fetchFutures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<PriceTick>> allTicksFuture = allOfFetchers
                .thenApply(v -> fetchFutures.stream()
                        .map(CompletableFuture::join) // We can .join() because we know they are all complete
                        .flatMap(List::stream)         // Flatten List<List<PriceTick>> into Stream<PriceTick>
                        .collect(Collectors.toList())
                );

        // 3. Chain the next step: save the ticks
        CompletableFuture<List<PriceTick>> saveFuture = allTicksFuture
                .thenApplyAsync(ticks -> {
                    log.debug("Saving {} ticks to database...", ticks.size());
                    ticks.forEach(databaseService::saveTick);
                    return ticks; // Pass the list of ticks down the chain
                }, executor);

        // 4. Chain the aggregate step
        CompletableFuture<Map<CurrencyPair, ConsolidatedPrice>> aggregateFuture = saveFuture
                .thenApplyAsync(ticks -> {
                    log.debug("Aggregating {} ticks...", ticks.size());
                    return priceService.aggregatePrices(ticks);
                }, executor);

        // 5. Chain the analyze step
        CompletableFuture<List<ArbitrageOpportunity>> arbitrageFuture = aggregateFuture
                .thenApplyAsync(priceMap -> {
                    log.debug("Analyzing price map for arbitrage...");
                    return arbitrageService.findArbitrageOpportunities(priceMap);
                }, executor);

        // 6. Chain the collect step (this is the end of the data pipeline)
        CompletableFuture<Void> collectFuture = arbitrageFuture
                .thenAcceptAsync(opportunities -> {
                    log.debug("Collecting {} new opportunities...", opportunities.size());
                    opportunities.forEach(opportunityAggregator::addOpportunity);
                }, executor);

        // 7. Add a final handler for any failure *in the chain*
        return collectFuture.exceptionally(ex -> {
            log.error("Fatal error in async processing pipeline: {}", ex.getMessage(), ex);
            return null; // Return null to complete the Void future
        });
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