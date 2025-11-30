package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
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
 */
@Service
public class PriceServiceImpl implements PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);

    private final List<PriceFetcher> fetchers;

    /**
     * Constructor with dependency injection.
     * Following Dependency Inversion Principle.
     *
     * @param fetchers List of price fetchers to aggregate from
     */
    public PriceServiceImpl(List<PriceFetcher> fetchers) {
        this.fetchers = fetchers != null ? fetchers : new ArrayList<>();
        log.info("PriceServiceImpl initialized with {} fetchers", this.fetchers.size());
    }

    @Override
    public Optional<AggregatedTopOfBookQuote> getAggregatedTopOfBookQuote(CurrencyPair pair) {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }

        log.debug("Fetching AggregatedTopOfBookQuote for {}", pair);

        List<PriceTick> successfulTicks = new ArrayList<>();

        // Sequential fetching (KISS principle - Phase 2 will add concurrency)
        for (PriceFetcher fetcher : fetchers) {
            try {
                PriceTick tick = fetcher.fetchPrice(pair);
                successfulTicks.add(tick);
                log.debug("Fetched from {}: bestBid={}, bestAsk={}",
                        fetcher.getExchange(), tick.bid(), tick.ask());
            } catch (PriceFetchException e) {
                // Gracefully skip failed fetchers (resilience)
                log.warn("Failed to fetch from {}: {}",
                        fetcher.getExchange(), e.getMessage());
            }
        }

        // If all fetchers failed, return empty
        if (successfulTicks.isEmpty()) {
            log.warn("No successful price fetches for {}", pair);
            return Optional.empty();
        }

        // Aggregate: Best bestBid (max), Best bestAsk (min)
        AggregatedTopOfBookQuote aggregatedTopOfBookQuote = aggregateTicks(pair, successfulTicks);
        log.info("AggregatedTopOfBookQuote for {}: bestBid={}, bestAsk={}",
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
