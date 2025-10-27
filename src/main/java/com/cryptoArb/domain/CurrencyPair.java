package com.cryptoArb.domain;


/**
 * Represents an immutable currency pair (e.g., BTC/USD).
 * As a record, this automatically includes:
 * - A public constructor (CurrencyPair_old(String base, String quote))
 * - Public accessor methods (base() and quote())
 * - Implementations for equals(), hashCode(), and toString()
 */
public record CurrencyPair(String base, String quote) implements Comparable<CurrencyPair> {


    @Override
    public String toString() {
        return "CurrencyPair[" +
                "base='" + base + '\'' +
                ", quote='" + quote + '\'' +
                ']';
    }

    /**
     * Compares this CurrencyPair to another, for sorting.
     * Sorts alphabetically by base currency first, then by quote currency.
     *
     * @param other the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(CurrencyPair other) {
        // First, try to compare by the base currency
        int baseComparison = this.base.compareTo(other.base);

        // If the base currencies are not the same, return that result
        if (baseComparison != 0) {
            return baseComparison;
        }

        // If they *are* the same (e.g., "BTC"), then
        // return the comparison of the quote currencies
        return this.quote.compareTo(other.quote);
    }
}
