package com.cryptoArb.domainOld;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class PriceTick_old implements Serializable {

    // Add a version ID. This is a crucial best practice.
    private static final long serialVersionUID = 1L;


    private final CurrencyPair_old pair;
    private final Exchange_old exchangeOld;
    private final Instant timestamp;
    private final BigDecimal bidPrice;
    private final BigDecimal askPrice;

    public PriceTick_old(CurrencyPair_old pair, Exchange_old exchangeOld, Instant timestamp, BigDecimal bidPrice, BigDecimal askPrice) {
        this.pair = pair;
        this.exchangeOld = exchangeOld;
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    public CurrencyPair_old getPair() {
        return pair;
    }

    public Exchange_old getExchange() {
        return exchangeOld;
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

}
