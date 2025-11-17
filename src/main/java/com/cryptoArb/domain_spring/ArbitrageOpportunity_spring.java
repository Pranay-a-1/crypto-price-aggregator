package com.cryptoArb.domain_spring;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * JPA Entity representing a found arbitrage opportunity.
 *
 * This class is designed for persistence with Spring Data JPA.
 * It mirrors the 'ArbitrageOpportunity' record but uses JPA annotations
 * and embeddable components.
 */
@Getter
@Entity
@Table(name = "arbitrage_opportunity")
public class ArbitrageOpportunity_spring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We reuse the embeddable CurrencyPair_spring
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "base", column = @Column(name = "base_currency")),
            @AttributeOverride(name = "quote", column = @Column(name = "quote_currency"))
    })
    private CurrencyPair_spring pair;

    @Column(nullable = false)
    private Instant timestamp;

    // --- Buy Side ---
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "exchangeId", column = @Column(name = "buy_exchange_id"))
    })
    private Exchange_spring buyExchange;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal buyPrice;

    // --- Sell Side ---
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "exchangeId", column = @Column(name = "sell_exchange_id"))
    })
    private Exchange_spring sellExchange;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal sellPrice;

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal profitPercentage;

    // JPA requires a no-arg constructor
    protected ArbitrageOpportunity_spring() {
    }

    // Full constructor for our application logic
    public ArbitrageOpportunity_spring(CurrencyPair_spring pair, Instant timestamp,
                                       Exchange_spring buyExchange, BigDecimal buyPrice,
                                       Exchange_spring sellExchange, BigDecimal sellPrice,
                                       BigDecimal profitPercentage) {
        this.pair = pair;
        this.timestamp = timestamp;
        this.buyExchange = buyExchange;
        this.buyPrice = buyPrice;
        this.sellExchange = sellExchange;
        this.sellPrice = sellPrice;
        this.profitPercentage = profitPercentage;
    }

    // --- Getters ---
    // (JPA and other frameworks rely on standard getters)

    // --- equals() and hashCode() ---
    // Standard practice for JPA entities is to base equality
    // only on the @Id field.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbitrageOpportunity_spring that = (ArbitrageOpportunity_spring) o;
        // Only check equality on the ID, and ensure ID is not null
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // Use a fixed value for objects without an ID (transient)
        // and the ID's hashcode once it's persisted.
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}