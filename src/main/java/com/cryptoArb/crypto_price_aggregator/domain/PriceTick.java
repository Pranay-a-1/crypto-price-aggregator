package com.cryptoArb.crypto_price_aggregator.domain;

import java.math.BigDecimal;
import java.time.Instant;


/**
 * Immutable domain model representing a single price quote from one exchange.
 * Following SOLID principles:
 * - Single Responsibility: Only represents price data
 * - Open/Closed: Immutable record prevents modification
 * <p>
 * Using BigDecimal for financial precision (avoid floating-point errors).
 * Using record for immutability by default (defensive programming).
 */
public record PriceTick(
        CurrencyPair pair,
        Exchange exchange,
        BigDecimal bid,
        BigDecimal ask,
        Instant timestamp) {
    /**
     * Canonical constructor with validation (fail-fast principle).
     */
    public PriceTick {
        // Validate non-null fields
        if (pair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }
        if (exchange == null) {
            throw new IllegalArgumentException("Exchange cannot be null");
        }
        if (bid == null) {
            throw new IllegalArgumentException("Bid price cannot be null");
        }
        if (ask == null) {
            throw new IllegalArgumentException("Ask price cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        // Validate price constraints
        if (bid.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Bid price must be non-negative, got: " + bid);
        }
        if (ask.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ask price must be non-negative, got: " + ask);
        }

        // Market sanity check: In normal markets, bestBid (buy price) <= bestAsk (sell price)
        // However, in arbitrage scenarios, this might be violated across exchanges
        // For now, we enforce this at the individual tick level
        if (bid.compareTo(ask) > 0) {
            throw new IllegalArgumentException(
                    String.format("Bid price (%s) cannot be greater than bestAsk price (%s)", bid, ask));
        }
    }
}
