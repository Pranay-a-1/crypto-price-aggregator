package com.cryptoArb.javaImpl.domain_POJOs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class PriceTick_POJO implements Serializable {

    // Add a version ID. This is a crucial best practice.
    private static final long serialVersionUID = 1L;


    private final CurrencyPair_POJO pair;
    private final Exchange_POJO exchangeOld;
    private final Instant timestamp;
    private final BigDecimal bidPrice;
    private final BigDecimal askPrice;

    public PriceTick_POJO(CurrencyPair_POJO pair, Exchange_POJO exchangeOld, Instant timestamp, BigDecimal bidPrice, BigDecimal askPrice) {
        this.pair = pair;
        this.exchangeOld = exchangeOld;
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    public CurrencyPair_POJO getPair() {
        return pair;
    }

    public Exchange_POJO getExchange() {
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
