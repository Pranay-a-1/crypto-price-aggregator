package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.service.ManualConcurrentPriceEngine;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PriceService that aggregates prices from multiple fetchers.
 * <p>
 * Following SOLID principles:
 * - Single Responsibility: Only aggregates prices, doesn't fetch or expose
 * - Dependency Inversion: Depends on PriceFetcher abstraction via constructor
 * injection
 * - Open/Closed: New fetchers can be added without modifying this code
 * <p>
 * Aggregation Logic:
 * - Best Bid = MAX of all bids (highest price someone will pay)
 * - Best Ask = MIN of all asks (lowest price someone will sell)
 * <p>
 * Following KISS: Simple sequential fetching (Phase 2 will add concurrency)
 * <p>
 * <b>Phase 2 Update:</b> Now uses {@link ManualConcurrentPriceEngine} for parallel fetching.
 * <p>
 * Following SOLID principles:
 * - Single Responsibility: Aggregation logic only. Threading delegated to Engine.
 * - Open/Closed: Engine can be swapped (e.g., for a Reactor version) without changing aggregation logic.
 */
@Service
public class PriceServiceImpl implements PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);

    private final List<PriceFetcher> fetchers;
    private final ManualConcurrentPriceEngine executionEngine;

    /**
     * Constructor with dependency injection.
     * Following Dependency Inversion Principle.
     *
     * @param fetchers List of price fetchers to aggregate from
     */
    public PriceServiceImpl(List<PriceFetcher> fetchers) {
        this.fetchers = fetchers != null ? fetchers : new ArrayList<>();

        // Manual wiring of the engine for Phase 2.
        // Ideally, this should be injected, but we are demonstrating manual composition first.
        // We ensure pool size is at least 4 or matches the number of fetchers to maximize parallelism.
        int poolSize = Math.max(4, this.fetchers.size());
        this.executionEngine = new ManualConcurrentPriceEngine(poolSize);

        log.info("PriceServiceImpl initialized with {} fetchers and ManualConcurrentPriceEngine (pool={})",
                this.fetchers.size(), poolSize);
    }

    @Override
    public Optional<AggregatedTopOfBookQuote> getAggregatedTopOfBookQuote(CurrencyPair pair) {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }

        log.debug("Fetching AggregatedTopOfBookQuote for {}", pair);

        // DELEGATE: Use the engine to fetch prices in parallel
        // This replaces the sequential for-loop from Phase 1
        List<PriceTick> successfulTicks = executionEngine.fetchPrices(fetchers, pair);

        // If all fetchers failed, return empty
        if (successfulTicks.isEmpty()) {
            log.warn("No successful price fetches for {}", pair);
            return Optional.empty();
        }

        // Aggregate: Best bestBid (max), Best bestAsk (min)
        AggregatedTopOfBookQuote aggregatedTopOfBookQuote = aggregateTicks(pair, successfulTicks);

        log.info("Aggregated result for {}: bestBid={}, bestAsk={}",
                pair, aggregatedTopOfBookQuote.bestBid(), aggregatedTopOfBookQuote.bestAsk());

        return Optional.of(aggregatedTopOfBookQuote);
    }

    /**
     * Aggregates multiple price ticks into a AggregatedTopOfBookQuote.
     * Best bestBid = MAX (highest buy price)
     * Best bestAsk = MIN (lowest sell price)
     */
    private AggregatedTopOfBookQuote aggregateTicks(CurrencyPair pair, List<PriceTick> ticks) {
        PriceTick bestBidTick = ticks.stream()
                .max(Comparator.comparing(PriceTick::bid))
                .orElseThrow(() -> new IllegalStateException("No bids available"));

        PriceTick bestAskTick = ticks.stream()
                .min(Comparator.comparing(PriceTick::ask))
                .orElseThrow(() -> new IllegalStateException("No asks available"));

        return new AggregatedTopOfBookQuote(
                pair,
                bestBidTick.bid(),
                bestBidTick.exchange(),
                bestAskTick.ask(),
                bestAskTick.exchange(),
                Instant.now()
        );
    }
}
