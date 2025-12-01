package com.cryptoArb.crypto_price_aggregator.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable JPA component representing a currency pair.
 * Converted from record to satisfy JPA requirements.
 * 
 * Using Lombok @Data for getters/setters while maintaining validation.
 * JPA requires:
 * - No-arg constructor (provided by @NoArgsConstructor)
 * - Mutable fields (for proxy creation)
 * 
 * Following SOLID principles:
 * - Single Responsibility: Only represents currency pair data
 * - Immutability sacrificed for JPA compatibility
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPair {

    private String base;
    private String quote;

    /**
     * Factory method with validation (maintains fail-fast principle).
     * Use this instead of constructor to ensure validation.
     */
    public static CurrencyPair of(String base, String quote) {
        // Defensive Coding: Fail fast if invalid data is attempted
        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("Base currency cannot be empty");
        }
        if (quote == null || quote.isBlank()) {
            throw new IllegalArgumentException("Quote currency cannot be empty");
        }
        // Normalize to uppercase to avoid "btc" vs "BTC" mismatch bugs
        return new CurrencyPair(base.toUpperCase(), quote.toUpperCase());
    }

    @Override
    public String toString() {
        return base + "/" + quote;
    }
}