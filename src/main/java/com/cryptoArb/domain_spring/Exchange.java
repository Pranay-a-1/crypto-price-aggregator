package com.cryptoArb.domain_spring;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Exchange {

    private String exchangeId;

    // JPA requires a no-arg constructor
    protected Exchange() {
    }

    public Exchange(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getId() {
        return exchangeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exchange exchangeSpring = (Exchange) o;
        return Objects.equals(exchangeId, exchangeSpring.exchangeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeId);
    }
}