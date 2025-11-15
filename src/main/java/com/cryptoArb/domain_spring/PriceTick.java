package com.cryptoArb.domain_spring;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;


/**
 * Represents a price snapshot from a specific exchange.
 * Converted to a record (Phase 3 task) for immutability and conciseness.
 *
 * @param pair       The currency pair (e.g., BTC/USD)
 * @param exchange   The exchange (e.g., coinbase)
 * @param timestamp  The epoch millisecond timestamp of the tick
 * @param bidPrice   The highest price a buyer is willing to pay
 * @param askPrice   The lowest price a seller is willing to accept
 * PHASE 11 UPDATE:
 * - Annotated as @Entity to be managed by JPA.
 * - @Table explicitly names the database table "price_tick".
 * - Added an @Id and @GeneratedValue for the primary key.
 * - CurrencyPair and Exchange are @Embedded, meaning their fields
 * will be columns in this table (e.g., "base", "quote", "id").
 */
@Getter
@Entity
@Table(name = "price_tick")
public class PriceTick {

    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CurrencyPair pair;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "exchange_id"))
    })
    private Exchange exchange;

    @Setter
    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal askPrice;

    // No-arg constructor required by JPA
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


    public void setPair(CurrencyPair pair) {
        this.pair = pair;
    }


    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }


    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }


    public void setAskPrice(BigDecimal askPrice) {
        this.askPrice = askPrice;
    }
}
