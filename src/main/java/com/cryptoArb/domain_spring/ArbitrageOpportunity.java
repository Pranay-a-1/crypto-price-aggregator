package com.cryptoArb.domain_spring;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Table(name = "arbitrage_opportunities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE arbitrage_opportunities SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ArbitrageOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CurrencyPair pair;

    private Instant timestamp;

    // --- Buy Side ---
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "exchangeId", column = @Column(name = "buy_exchange_id"))
    })
    private Exchange buyExchange;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal buyPrice;

    // --- Sell Side ---
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "exchangeId", column = @Column(name = "sell_exchange_id"))
    })
    private Exchange sellExchange;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal sellPrice;

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal profitPercentage;

    // JPA requires a no-arg constructor
    // protected ArbitrageOpportunity() {} // Lombok @NoArgsConstructor handles this

    // Full constructor for our application logic (Compatibility with tests)
    public ArbitrageOpportunity(CurrencyPair pair, Instant timestamp,
            Exchange buyExchange, BigDecimal buyPrice,
            Exchange sellExchange, BigDecimal sellPrice,
            BigDecimal profitPercentage) {
        this.pair = pair;
        this.timestamp = timestamp;
        this.buyExchange = buyExchange;
        this.buyPrice = buyPrice;
        this.sellExchange = sellExchange;
        this.sellPrice = sellPrice;
        this.profitPercentage = profitPercentage;
    }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastModifiedDate;

    @Column(nullable = false)
    private boolean deleted = false;

    // --- equals() and hashCode() ---
    // Standard practice for JPA entities is to base equality
    // only on the @Id field.

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArbitrageOpportunity that = (ArbitrageOpportunity) o;
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