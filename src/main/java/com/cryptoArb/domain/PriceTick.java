package com.cryptoArb.domain;

import java.math.BigDecimal;

public record PriceTick(
        CurrencyPair pair,
        Exchange exchange,
        long timestamp,
        BigDecimal bidPrice,
        BigDecimal askPrice
) {



}
