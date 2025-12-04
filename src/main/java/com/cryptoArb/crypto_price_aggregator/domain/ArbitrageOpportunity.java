package com.cryptoArb.crypto_price_aggregator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA Entity representing a detected arbitrage opportunity.
 * 
 * An arbitrage opportunity exists when you can buy an asset on one exchange
 * at a lower price and simultaneously sell it on another exchange at a higher
 * price, making a risk-free profit.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Only represents arbitrage opportunity data
 * - Immutable business logic via validation
 * 
 * Using BigDecimal for financial precision (avoid floating-point errors).
 * 
 * TDD: This class was developed test-first, with validation logic driven by
 * tests.
 */
@Getter
@Entity
@Table(name = "arbitrage_opportunities", indexes = {
        @Index(name = "idx_detected_at", columnList = "detected_at"),
        @Index(name = "idx_currency_pair", columnList = "base_currency,quote_currency")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrageOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The currency pair for this arbitrage opportunity.
     * Embedded to denormalize and avoid joins for common queries.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "base", column = @Column(name = "base_currency", nullable = false)),
            @AttributeOverride(name = "quote", column = @Column(name = "quote_currency", nullable = false))
    })
    private CurrencyPair currencyPair;

    /**
     * Exchange where we should BUY the asset (lower price).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "buy_exchange", nullable = false)
    private Exchange buyExchange;

    /**
     * Exchange where we should SELL the asset (higher price).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sell_exchange", nullable = false)
    private Exchange sellExchange;

    /**
     * Price to buy at (should be lower than sell price).
     */
    @Column(name = "buy_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal buyPrice;

    /**
     * Price to sell at (should be higher than buy price).
     */
    @Column(name = "sell_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal sellPrice;

    /**
     * Profit percentage from this arbitrage opportunity.
     * Formula: ((sellPrice - buyPrice) / buyPrice) * 100
     */
    @Column(name = "profit_percentage", nullable = false, precision = 10, scale = 4)
    private BigDecimal profitPercentage;

    /**
     * Timestamp when this opportunity was detected.
     */
    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    /**
     * Constructor without ID (for creating new instances before persistence).
     * Includes validation (fail-fast principle).
     */
    public ArbitrageOpportunity(
            CurrencyPair currencyPair,
            Exchange buyExchange,
            Exchange sellExchange,
            BigDecimal buyPrice,
            BigDecimal sellPrice,
            BigDecimal profitPercentage,
            Instant detectedAt) {

        validateFields(currencyPair, buyExchange, sellExchange, buyPrice, sellPrice, profitPercentage, detectedAt);

        this.currencyPair = currencyPair;
        this.buyExchange = buyExchange;
        this.sellExchange = sellExchange;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.profitPercentage = profitPercentage;
        this.detectedAt = detectedAt;
    }

    /**
     * Validation method to ensure data integrity.
     * Arbitrage logic: Buy low (buyPrice) on one exchange, sell high (sellPrice) on
     * another.
     */
    private void validateFields(
            CurrencyPair currencyPair,
            Exchange buyExchange,
            Exchange sellExchange,
            BigDecimal buyPrice,
            BigDecimal sellPrice,
            BigDecimal profitPercentage,
            Instant detectedAt) {

        // Validate non-null fields
        if (currencyPair == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }
        if (buyExchange == null || sellExchange == null) {
            throw new IllegalArgumentException("Exchange cannot be null");
        }
        if (buyPrice == null || sellPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (profitPercentage == null) {
            throw new IllegalArgumentException("Profit percentage cannot be null");
        }
        if (detectedAt == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        // Validate price constraints for arbitrage
        if (buyPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Buy price must be non-negative, got: " + buyPrice);
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sell price must be non-negative, got: " + sellPrice);
        }

        // Core arbitrage validation: buy price must be less than sell price
        if (buyPrice.compareTo(sellPrice) >= 0) {
            throw new IllegalArgumentException(
                    String.format("Buy price must be less than sell price for arbitrage. Buy: %s, Sell: %s",
                            buyPrice, sellPrice));
        }

        // Validate profit percentage is positive
        if (profitPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Profit percentage must be positive, got: " + profitPercentage);
        }
    }
}
