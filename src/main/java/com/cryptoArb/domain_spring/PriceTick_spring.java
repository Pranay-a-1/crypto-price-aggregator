package com.cryptoArb.domain_spring;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "price_tick")
public class PriceTick_spring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "base", column = @Column(name = "base_currency")),
            @AttributeOverride(name = "quote", column = @Column(name = "quote_currency"))
    })
    private CurrencyPair_spring pair;

    @Embedded
    @AttributeOverride(name = "exchangeId", column = @Column(name = "exchange_id"))
    private Exchange_spring exchangeSpring;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal askPrice;

    // JPA requires a no-arg constructor
    protected PriceTick_spring() {
    }

    public PriceTick_spring(CurrencyPair_spring pair, Exchange_spring exchangeSpring, Instant timestamp,
                            BigDecimal bidPrice, BigDecimal askPrice) {
        this.pair = pair;
        this.exchangeSpring = exchangeSpring;
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public CurrencyPair_spring getPair() {
        return pair;
    }

    public Exchange_spring getExchange() {
        return exchangeSpring;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceTick_spring priceTickSpring = (PriceTick_spring) o;
        return Objects.equals(id, priceTickSpring.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}