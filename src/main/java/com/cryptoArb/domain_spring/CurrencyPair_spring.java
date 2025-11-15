package com.cryptoArb.domain_spring;


import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class CurrencyPair_spring {

    private String base;
    private String quote;

    // JPA requires a no-arg constructor
    protected CurrencyPair_spring() {
    }

    public CurrencyPair_spring(String base, String quote) {
        this.base = base;
        this.quote = quote;
    }

    public String getBase() {
        return base;
    }

    public String getQuote() {
        return quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyPair_spring that = (CurrencyPair_spring) o;
        return Objects.equals(base, that.base) &&
                Objects.equals(quote, that.quote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, quote);
    }
}