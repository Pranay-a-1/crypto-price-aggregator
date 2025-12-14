package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
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
import java.util.HashMap;

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
    
    // Minimum interval between external API calls (5 seconds) to avoid rate limiting
    private static final long CACHE_VALIDITY_MS = 5000;

    private final List<PriceFetcher> fetchers;
    private final ManualConcurrentPriceEngine executionEngine;
    private final PriceTickRepository repository;
    
    // Simple in-memory cache to rate-limit external API calls
    private final Map<CurrencyPair, CachedPriceTicks> priceCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Simple cache entry holding price ticks and their fetch timestamp.
     */
    private static class CachedPriceTicks {
        final List<PriceTick> ticks;
        final long fetchedAtMs;
        
        CachedPriceTicks(List<PriceTick> ticks) {
            this.ticks = ticks;
            this.fetchedAtMs = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - fetchedAtMs > CACHE_VALIDITY_MS;
        }
    }

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
    
    /**
     * Fetches price ticks using a cache to rate-limit external API calls.
     * External APIs are only called if the cache is expired (>5 seconds old).
     * This prevents hitting third-party APIs too frequently and avoids rate limits.
     *
     * @param pair The currency pair to fetch prices for
     * @return List of price ticks (may be from cache)
     */
    private List<PriceTick> getCachedPriceTicks(CurrencyPair pair) {
        CachedPriceTicks cached = priceCache.get(pair);
        
        // If cache exists and is not expired, use cached data
        if (cached != null && !cached.isExpired()) {
            log.debug("Using cached price ticks for {} (age: {}ms)", 
                    pair, System.currentTimeMillis() - cached.fetchedAtMs);
            return cached.ticks;
        }
        
        // Cache is expired or doesn't exist - fetch fresh data
        log.debug("Cache miss or expired for {}, fetching from external APIs", pair);
        List<PriceTick> freshTicks = executionEngine.fetchPrices(fetchers, pair);
        
        // Only cache if we got valid data
        if (!freshTicks.isEmpty()) {
            priceCache.put(pair, new CachedPriceTicks(freshTicks));
            log.debug("Cached {} price ticks for {}", freshTicks.size(), pair);
        }
        
        return freshTicks;
    }

    @Override
    public Optional<AggregatedTopOfBookQuote> getAggregatedTopOfBookQuote(CurrencyPair pair) {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }

        log.debug("Fetching AggregatedTopOfBookQuote for {}", pair);

        // Use cached price ticks to avoid rate limiting from external APIs
        // Fresh API calls are only made if cache is expired (>5 seconds old)
        List<PriceTick> freshTicks = getCachedPriceTicks(pair);

        if (freshTicks.isEmpty()) {
            // Fallback to DB if live fetch fails? Or just return empty?
            // For real-time aggregator, we prefer live data.
            // If fetch failed, checking DB might give stale data, but let's check DB just in case?
            // Actually, "In-Memory Persistence" logic (Phase 3) preferred DB for aggregation.
            // But with Async MQ, DB is eventually consistent.
            // We'll stick to freshTicks for "Real-Time" top of book.
            log.warn("No fresh price ticks obtained for {}", pair);
            return Optional.empty();
        }

        // Filter to get only the latest tick from each exchange (in case engine returns multiples, though it usually returns one per fetcher)
        Map<Exchange, PriceTick> latestTicksByExchange = new HashMap<>();
        for (PriceTick tick : freshTicks) {
            // merge method will keep the tick with the latest timestamp
            latestTicksByExchange.merge(tick.getExchange(), tick,
                (existing, replacement) -> replacement.getTimestamp().isAfter(existing.getTimestamp()) ? replacement : existing);
        }

        List<PriceTick> latestTicks = new ArrayList<>(latestTicksByExchange.values());
        log.debug("Number of latest ticks after filtering: {}", latestTicks.size());
        log.debug("Latest ticks details: {}", latestTicks);

        // If no latest ticks found after filtering, return empty
        if (latestTicks.isEmpty()) {
            log.warn("No latest price ticks found for {} after filtering by exchange",
                    pair);
            return Optional.empty();
        }

        // Aggregate from latest ticks (one per exchange)
        AggregatedTopOfBookQuote aggregatedTopOfBookQuote = aggregateTicks(pair, latestTicks);

        log.info("Aggregated result for {} from {} latest ticks (one per exchange): bestBid={} (exchange={}), bestAsk={} (exchange={})",
                pair, latestTicks.size(), aggregatedTopOfBookQuote.bestBid(), aggregatedTopOfBookQuote.bestBidExchange(),
                aggregatedTopOfBookQuote.bestAsk(), aggregatedTopOfBookQuote.bestAskExchange());

        return Optional.of(aggregatedTopOfBookQuote);
    }



    @Override
    public Map<String, PriceTick> getLatestPriceTicks(CurrencyPair pair) {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }

        log.debug("Fetching latest price ticks for {}", pair);

        // Use cached price ticks to avoid rate limiting from external APIs
        List<PriceTick> freshTicks = getCachedPriceTicks(pair);

        // Convert to Map<ExchangeName, PriceTick>
        return freshTicks.stream()
                .collect(Collectors.toMap(
                        tick -> tick.getExchange().name(),
                        tick -> tick,
                        (existing, replacement) -> replacement // Keep the latest/last one if duplicates exist
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
