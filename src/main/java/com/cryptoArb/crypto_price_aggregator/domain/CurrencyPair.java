package com.cryptoArb.crypto_price_aggregator.domain;

public record CurrencyPair(String base, String quote) {

    public CurrencyPair {
        // Defensive Coding: Fail fast if invalid data is attempted
        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("Base currency cannot be empty");
        }
        if (quote == null || quote.isBlank()) {
            throw new IllegalArgumentException("Quote currency cannot be empty");
        }
        // Normalize to uppercase to avoid "btc" vs "BTC" mismatch bugs
        base = base.toUpperCase();
        quote = quote.toUpperCase();
    }

    @Override
    public String toString() {
        return base + "/" + quote;
    }
}