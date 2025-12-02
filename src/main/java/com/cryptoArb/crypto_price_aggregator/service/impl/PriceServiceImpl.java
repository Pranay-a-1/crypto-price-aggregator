package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import com.cryptoArb.crypto_price_aggregator.service.ManualConcurrentPriceEngine;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
 * <b>Phase 2 Update:</b> Now uses {@link ManualConcurrentPriceEngine} for
 * parallel fetching.
 * <p>
 * <b>Phase 3 Update:</b> Now persists fetched ticks to H2 database via
 * {@link PriceTickRepository}.
 * Aggregation queries recent ticks from database (last 5 seconds) for better
 * data consistency.
 * <p>
 * Following SOLID principles:
 * - Single Responsibility: Aggregation logic only. Threading delegated to
 * Engine, persistence to Repository.
 * - Open/Closed: Engine and Repository can be swapped without changing
 * aggregation logic.
 */
@Service
@Transactional
public class PriceServiceImpl implements PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);

    // Time window for considering "recent" ticks (5 seconds)
    private static final int RECENT_TICKS_WINDOW_SECONDS = 5;

    private final List<PriceFetcher> fetchers;
    private final ManualConcurrentPriceEngine executionEngine;
    private final PriceTickRepository repository;

    /**
     * Constructor that receives runtime dependencies.
     * <p>
     * <b>Phase 3 Update:</b> Now injects {@link PriceTickRepository} for
     * persistence.
     *
     * @param fetchers   List of price fetchers to aggregate from (may be null)
     * @param repository Repository for persisting and querying price ticks
     */
    public PriceServiceImpl(List<PriceFetcher> fetchers, PriceTickRepository repository) {
        // Ensure fetchers is never null to simplify usage elsewhere
        this.fetchers = fetchers != null ? fetchers : new ArrayList<>();
        this.repository = repository;

        // Manually create the execution engine for parallel fetching.
        int poolSize = Math.max(4, this.fetchers.size());
        this.executionEngine = new ManualConcurrentPriceEngine(poolSize);

        log.info(
                "PriceServiceImpl initialized with {} fetchers, ManualConcurrentPriceEngine (pool={}), and PriceTickRepository",
                this.fetchers.size(), poolSize);
    }

    @Override
    public Optional<AggregatedTopOfBookQuote> getAggregatedTopOfBookQuote(CurrencyPair pair) {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }

        log.debug("Fetching AggregatedTopOfBookQuote for {}", pair);

        // PHASE 3: Fetch prices in parallel using the engine
        List<PriceTick> freshTicks = executionEngine.fetchPrices(fetchers, pair);

        // PHASE 3: Save all fetched ticks to database
        if (!freshTicks.isEmpty()) {
            repository.saveAll(freshTicks);
            log.debug("Saved {} fresh ticks to database for {}", freshTicks.size(), pair);
        }

        // PHASE 3: Query recent ticks from database (last 5 seconds)
        Instant cutoff = Instant.now().minusSeconds(RECENT_TICKS_WINDOW_SECONDS);
        List<PriceTick> recentTicks = repository.findByPair_BaseAndPair_QuoteAndTimestampAfter(
                pair.getBase(), pair.getQuote(), cutoff);

        // If no recent ticks found in database, return empty
        if (recentTicks.isEmpty()) {
            log.warn("No recent price ticks found for {} in the last {} seconds",
                    pair, RECENT_TICKS_WINDOW_SECONDS);
            return Optional.empty();
        }

        // Aggregate from database ticks
        AggregatedTopOfBookQuote aggregatedTopOfBookQuote = aggregateTicks(pair, recentTicks);

        log.info("Aggregated result for {} from {} recent ticks: bestBid={} (exchange={}), bestAsk={} (exchange={})",
                pair, recentTicks.size(), aggregatedTopOfBookQuote.bestBid(), aggregatedTopOfBookQuote.bestBidExchange(),
                aggregatedTopOfBookQuote.bestAsk(), aggregatedTopOfBookQuote.bestAskExchange());

        return Optional.of(aggregatedTopOfBookQuote);
    }



    @Override
    public Map<String, PriceTick> getLatestPriceTicks(CurrencyPair pair) {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }

        log.debug("Fetching latest price ticks for {}", pair);

        // Fetch prices using the engine (same as getAggregatedTopOfBookQuote)
        List<PriceTick> freshTicks = executionEngine.fetchPrices(fetchers, pair);

        // Convert to Map<ExchangeName, PriceTick>
        return freshTicks.stream()
                .collect(Collectors.toMap(
                        tick -> tick.getExchange().name(),
                        tick -> tick
                ));
    }

    /**
     * Aggregates multiple price ticks into a AggregatedTopOfBookQuote.
     * Best bestBid = MAX (highest buy price)
     * Best bestAsk = MIN (lowest sell price)
     */
    private AggregatedTopOfBookQuote aggregateTicks(CurrencyPair pair, List<PriceTick> ticks) {
        PriceTick bestBidTick = ticks.stream()
                .max(Comparator.comparing(PriceTick::getBid))
                .orElseThrow(() -> new IllegalStateException("No bids available"));

        PriceTick bestAskTick = ticks.stream()
                .min(Comparator.comparing(PriceTick::getAsk))
                .orElseThrow(() -> new IllegalStateException("No asks available"));

        return new AggregatedTopOfBookQuote(
                pair,
                bestBidTick.getBid(),
                bestBidTick.getExchange(),
                bestAskTick.getAsk(),
                bestAskTick.getExchange(),
                Instant.now());
    }
}
