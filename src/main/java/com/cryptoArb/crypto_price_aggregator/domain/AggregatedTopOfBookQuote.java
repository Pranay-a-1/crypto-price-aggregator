package com.cryptoArb.crypto_price_aggregator.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record AggregatedTopOfBookQuote(
        CurrencyPair pair,
        BigDecimal bestBid,
        Exchange bestBidExchange,
        BigDecimal bestAsk,
        Exchange bestAskExchange,
        Instant timestamp
) {
    public AggregatedTopOfBookQuote {
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }
        if (bestBid == null || bestBid.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Bid price must be non-negative");
        }
        if (bestAsk == null || bestAsk.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ask price must be non-negative");
        }
        if (bestBidExchange == null) {
            throw new IllegalArgumentException("BestBidExchange cannot be null");
        }
        if (bestAskExchange == null) {
            throw new IllegalArgumentException("BestAskExchange cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        // Valid business logic: Bid (what people pay) usually shouldn't be higher than Ask (what people sell for)
        // in a AggregatedTopOfBookQuote view, but in arbitrage, it might be.
        // For now, we strictly validate inputs, not market logic.
    }
}