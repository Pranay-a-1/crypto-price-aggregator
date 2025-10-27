package com.cryptoArb.domain;


/**
 * Represents an immutable currency pair (e.g., BTC/USD).
 * As a record, this automatically includes:
 * - A public constructor (CurrencyPair_old(String base, String quote))
 * - Public accessor methods (base() and quote())
 * - Implementations for equals(), hashCode(), and toString()
 */
public record CurrencyPair(String base, String quote) {
    @Override
    public String toString() {
        return "CurrencyPair[" +
                "base='" + base + '\'' +
                ", quote='" + quote + '\'' +
                ']';
    }
    // The body can be empty for this simple case.
}
