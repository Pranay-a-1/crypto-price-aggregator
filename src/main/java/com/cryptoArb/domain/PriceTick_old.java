package com.cryptoArb.domain;

public class PriceTick_old {


    private final CurrencyPair_old pair;
    private final Exchange_old exchangeOld;
    private final long timestamp;
    private final double bidPrice;
    private final double askPrice;

    public PriceTick_old(CurrencyPair_old pair, Exchange_old exchangeOld, long timestamp, double bidPrice, double askPrice) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public double getAskPrice() {
        return askPrice;
    }

}
