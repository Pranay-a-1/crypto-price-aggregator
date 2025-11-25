package com.cryptoArb.domain_spring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * CurrencyPair represents a trading pair (e.g., BTC/USD).
 * This is an embeddable entity used in PriceTick and ArbitrageOpportunity.
 * 
 * Jackson annotations ensure proper JSON serialization/deserialization for
 * RabbitMQ messaging.
 */
@Embeddable
public class CurrencyPair {

    private String base;
    private String quote;

    // JPA requires a no-arg constructor
    protected CurrencyPair() {
    }

    @JsonCreator
    public CurrencyPair(@JsonProperty("base") String base, @JsonProperty("quote") String quote) {
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CurrencyPair that = (CurrencyPair) o;
        return Objects.equals(base, that.base) &&
                Objects.equals(quote, that.quote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, quote);
    }
}