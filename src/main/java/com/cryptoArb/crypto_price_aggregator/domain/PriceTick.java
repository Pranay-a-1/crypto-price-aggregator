package com.cryptoArb.crypto_price_aggregator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA Entity representing a single price quote from one exchange.
 * Converted from record to satisfy JPA requirements.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Only represents price data
 * - JPA requires mutable fields and no-arg constructor
 * 
 * Using BigDecimal for financial precision (avoid floating-point errors).
 */
@Getter
@Entity
@Table(name = "price_ticks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceTick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CurrencyPair pair;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal bid;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal ask;

    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Constructor without ID (for creating new instances before persistence).
     * Includes validation (fail-fast principle).
     */
    public PriceTick(CurrencyPair pair, Exchange exchange, BigDecimal bid, BigDecimal ask, Instant timestamp) {
        validateFields(pair, exchange, bid, ask, timestamp);
        this.pair = pair;
        this.exchange = exchange;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    /**
     * Validation method to ensure data integrity.
     */
    private void validateFields(CurrencyPair pair, Exchange exchange, BigDecimal bid, BigDecimal ask,
            Instant timestamp) {
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

        // Market sanity check: In normal markets, bestBid (buy price) <= bestAsk (sell
        // price)
        if (bid.compareTo(ask) > 0) {
            throw new IllegalArgumentException(
                    String.format("Bid price (%s) cannot be greater than ask price (%s)", bid, ask));
        }
    }
}
