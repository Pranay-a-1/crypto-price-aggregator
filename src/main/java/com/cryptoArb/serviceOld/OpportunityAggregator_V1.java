package com.cryptoArb.serviceOld;

import com.cryptoArb.domain.ArbitrageOpportunity;

import java.util.ArrayList;
import java.util.List;

/**
 * A service responsible for safely collecting
 * arbitrage opportunities from concurrent threads.
 *
 * VERSION 1: Not thread-safe (RED)
 */
public class OpportunityAggregator_V1 {

    private final List<ArbitrageOpportunity> opportunities = new ArrayList<>();

    /**
     * Adds a found opportunity to the list.
     * WARNING: This is not thread-safe.
     *
     *
     * synchronized(this) locks the instance's monitor (the OpportunityAggregator object).
     * synchronized(opportunities) locks the List object's monitor (the opportunities list).
     * Consequences:
     *
     *
     * Different monitors = different locks. If some methods use synchronized(this) and others use synchronized(opportunities), they do not block each other and you lose mutual exclusion.
     * If opportunities is not final or can be replaced, synchronized(opportunities) can be broken; your code has it final, so that specific risk is avoided.
     * If opportunities is visible to external code, external synchronization on it can interact unexpectedly or cause deadlocks; using this avoids that particular exposure (but this can also be synchronized by callers if they have a reference).
     * synchronized(opportunities) will throw NPE if opportunities is null.
     * Recommendation: use a private final lock object for clarity and safety, or ensure every synchronized block uses the same monitor. Also make other readers (e.g., getOpportunityCount) use the same lock.
     *
     *
     */
    public void addOpportunity(ArbitrageOpportunity opportunity) {
        synchronized(this) {
            this.opportunities.add(opportunity);
        }
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
        synchronized(this) {
            // We return a new list to prevent modification of the original
            return new ArrayList<>(this.opportunities);
        }
    }

    public int getOpportunityCount() {
        return this.opportunities.size();
    }
}