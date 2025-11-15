package com.cryptoArb.core.internals;

import com.cryptoArb.domain_records.ConsolidatedPrice;
import com.cryptoArb.domain_records.CurrencyPair;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Demonstrates a memory-sensitive cache using WeakHashMap.
 * This fulfills Phase 9 of the project plan.
 */
public class PriceCache {

    // 1. The internal store is a WeakHashMap.
    // This map holds its keys (CurrencyPair) using "weak references".
    private final Map<CurrencyPair, ConsolidatedPrice> cache;

    /**
     * Initializes the cache.
     */
    public PriceCache() {
        // 2. We initialize it as a WeakHashMap, not a regular HashMap.
        this.cache = new WeakHashMap<>();
    }

    /**
     * Adds or updates an entry in the cache.
     * @param key The currency pair (held weakly)
     * @param value The consolidated price (held strongly by the map)
     */
    public void put(CurrencyPair key, ConsolidatedPrice value) {
        this.cache.put(key, value);
    }

    /**
     * Returns the current number of items in the cache.
     * This size can change if the GC runs.
     * @return The number of entries.
     */
    public int size() {
        return this.cache.size();
    }
}