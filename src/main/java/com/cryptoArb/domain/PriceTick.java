package com.cryptoArb.domain;

public record PriceTick(
        CurrencyPair pair,
        Exchange exchange,
        long timestamp,
        double bidPrice,
        double askPrice
) {
}
