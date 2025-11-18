package com.cryptoArb.domain_spring;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;


/**
 * PriceTick class represents a price tick for a specific currency pair and exchange.
 * It is used to store the price tick data in the database.
 *
 * It has the following fields:
 * 1. id: The primary key of the price tick.
 * 2. pair: The currency pair for which the price tick is stored.
 * 3. exchange: The exchange where the price tick is stored.
 * 4. timestamp: The timestamp of the price tick.
 * 5. bidPrice: The bid price of the price tick.
 * 6. askPrice: The ask price of the price tick.
 */
@Entity
@Table(name = "price_tick")
public class PriceTick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "base", column = @Column(name = "base_currency")),
            @AttributeOverride(name = "quote", column = @Column(name = "quote_currency"))
    })
    private CurrencyPair pair;

    @Embedded
    @AttributeOverride(name = "exchangeId", column = @Column(name = "exchange_id"))
    private Exchange exchange;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal askPrice;

    // JPA requires a no-arg constructor
    protected PriceTick() {
    }

    public PriceTick(CurrencyPair pair, Exchange exchange, Instant timestamp,
                     BigDecimal bidPrice, BigDecimal askPrice) {
        this.pair = pair;
        this.exchange = exchange;
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public Exchange getExchange() {
        return exchange;
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
        PriceTick priceTickSpring = (PriceTick) o;
        return Objects.equals(id, priceTickSpring.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}