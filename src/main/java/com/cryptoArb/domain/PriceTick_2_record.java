package com.cryptoArb.domain;

public record PriceTick_2_record(
        CurrencyPair_2_record pair,
        Exchange_2_record exchange,
        long timestamp,
        double bidPrice,
        double askPrice
) {
}
