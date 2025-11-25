package com.cryptoArb.domain_spring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Exchange represents a cryptocurrency exchange platform.
 * This is an embeddable entity used in PriceTick and ArbitrageOpportunity.
 * 
 * Jackson annotations ensure proper JSON serialization/deserialization for
 * RabbitMQ messaging.
 */
@Embeddable
public class Exchange {

    @JsonProperty("id")
    private String exchangeId;

    // JPA requires a no-arg constructor
    protected Exchange() {
    }

    @JsonCreator
    public Exchange(@JsonProperty("id") String exchangeId) {
        this.exchangeId = exchangeId;
    }

    @JsonProperty("id")
    public String getId() {
        return exchangeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Exchange exchange = (Exchange) o;
        return Objects.equals(exchangeId, exchange.exchangeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeId);
    }
}