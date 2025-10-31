package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;

import java.util.ArrayList;
import java.util.List;

/**
 * A service responsible for safely collecting
 * arbitrage opportunities from concurrent threads.
 *
 * VERSION 1: Not thread-safe (RED)
 */
public class OpportunityAggregator {

    private final List<ArbitrageOpportunity> opportunities = new ArrayList<>();

    /**
     * Adds a found opportunity to the list.
     * WARNING: This is not thread-safe.
     */
    public void addOpportunity(ArbitrageOpportunity opportunity) {
        this.opportunities.add(opportunity);
    }

    /**
     * Returns a snapshot of all found opportunities.
     */

    /**
     *
     * does the retun protect against side effects?
     * Short answer: partly.
     *
     * - `return new ArrayList<>(this.opportunities);` prevents the caller from modifying the internal list structure (adds/removes on the returned list do not affect `opportunities`).
     * - It does *not* protect against:
     *   - Mutating the objects inside the list (shallow copy). If `ArbitrageOpportunity` is mutable, callers can change shared objects.
     *   - Concurrent modification while copying. Since the class is not thread\-safe, a concurrent `addOpportunity` can cause inconsistent snapshot or `ConcurrentModificationException`.
     *
     * If you need full protection, either:
     * - Make `ArbitrageOpportunity` immutable, or return deep copies:
     * ```java
     * // example: deep copy snapshot (if ArbitrageOpportunity has a copy constructor or clone)
     * public List<ArbitrageOpportunity> getOpportunities() {
     *     List<ArbitrageOpportunity> snapshot = new ArrayList<>();
     *     synchronized (this) {
     *         for (ArbitrageOpportunity o : this.opportunities) {
     *             snapshot.add(new ArbitrageOpportunity(o)); // or o.copy()
     *         }
     *     }
     *     return Collections.unmodifiableList(snapshot);
     * }
     * ```
     * - Or ensure thread-safety with a concurrent collection:
     * ```java
     * private final List<ArbitrageOpportunity> opportunities = new CopyOnWriteArrayList<>();
     * public List<ArbitrageOpportunity> getOpportunities() {
     *     return Collections.unmodifiableList(new ArrayList<>(opportunities));
     * }
     * ```
     * Choose immutability or deep copies to avoid element-level side effects; choose synchronization or concurrent collections to avoid race conditions.
     *
     *
     *
     *
     */
    public List<ArbitrageOpportunity> getOpportunities() {
        // We return a new list to prevent modification of the original
        return new ArrayList<>(this.opportunities);
    }

    public int getOpportunityCount() {
        return this.opportunities.size();
    }
}