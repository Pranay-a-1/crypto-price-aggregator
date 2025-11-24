package com.cryptoArb.domain_spring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents static metadata about an exchange.
 * We define this as a separate Entity to demonstrate JPA relationships and Caching.
 */
@Getter
@Entity
@Table(name = "exchange_info")
public class ExchangeInfo {

    @Id
    @Column(name = "exchange_id")
    private String id; // e.g., "coinbase"

    private String fullName;
    private String url;

    @Column(name = "trading_fee_percent")
    private Double tradingFee;

    // JPA requires no-arg constructor
    protected ExchangeInfo() {}

    public ExchangeInfo(String id, String fullName, String url, Double tradingFee) {
        this.id = id;
        this.fullName = fullName;
        this.url = url;
        this.tradingFee = tradingFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeInfo that = (ExchangeInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}