package com.cryptoArb.domain;

public class PriceTick {


    private final CurrencyPair pair;
    private final Exchange exchange;
    private final long timestamp;
    private final double bidPrice;
    private final double askPrice;

    public PriceTick(CurrencyPair pair, Exchange exchange, long timestamp, double bidPrice, double askPrice) {
        this.pair = pair;
        this.exchange = exchange;
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public Exchange getExchange() {
        return exchange;
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
