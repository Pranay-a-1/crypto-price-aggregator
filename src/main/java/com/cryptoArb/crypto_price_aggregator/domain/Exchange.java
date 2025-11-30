package com.cryptoArb.crypto_price_aggregator.domain;

import lombok.Getter;

/**
 * Enum representing cryptocurrency exchanges.
 * Following KISS principle: Simple enum without complex logic.
 * DRY principle: Single source of truth for exchange names.
 */
@Getter
public enum Exchange {
    BINANCE("Binance"),
    COINBASE("Coinbase"),
    KRAKEN("Kraken"),
    MOCK("Mock Exchange");

    private final String displayName;

    Exchange(String displayName) {
        this.displayName = displayName;
    }

}
