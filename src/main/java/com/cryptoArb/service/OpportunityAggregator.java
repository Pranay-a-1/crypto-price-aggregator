package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;

import java.util.ArrayList;
import java.util.List;

/**
 * A service responsible for safely collecting
 * arbitrage opportunities from concurrent threads.
 *
 * VERSION 2: Thread-safe using 'synchronized' (GREEN)
 *
 *
 *  *
 * synchronized(this) locks the instance's monitor (the OpportunityAggregator object).
 * synchronized(opportunities) locks the List object's monitor (the opportunities list).
 *
 * Consequences:
 * Different monitors = different locks. If some methods use synchronized(this) and others use synchronized(opportunities), they do not block each other and you lose mutual exclusion.
 * If opportunities is not final or can be replaced, synchronized(opportunities) can be broken; your code has it final, so that specific risk is avoided.
 * If opportunities is visible to external code, external synchronization on it can interact unexpectedly or cause deadlocks; using this avoids that particular exposure (but this can also be synchronized by callers if they have a reference).
 * synchronized(opportunities) will throw NPE if opportunities is null.
 * Recommendation: use a private final lock object for clarity and safety, or ensure every synchronized block uses the same monitor. Also make other readers (e.g., getOpportunityCount) use the same lock.
 */
public class OpportunityAggregator {

    private final List<ArbitrageOpportunity> opportunities = new ArrayList<>();

    /**
     * Adds a found opportunity to the list.
     * This is now thread-safe.
     */
    public void addOpportunity(ArbitrageOpportunity opportunity) {
        // Only one thread at a time can enter this block
        // because they must "acquire the lock" on the 'opportunities' list
        synchronized (opportunities) {
            this.opportunities.add(opportunity);
        }
    }

    /**
     * Returns a snapshot of all found opportunities.
     * This is also synchronized to ensure we read safely.
     */
    public List<ArbitrageOpportunity> getOpportunities() {
        // We must also lock here to safely read the list
        // while another thread might be trying to write
        synchronized (opportunities) {
            return new ArrayList<>(this.opportunities);
        }
    }

    public int getOpportunityCount() {
        // The .size() method also needs to be protected
        synchronized (opportunities) {
            return this.opportunities.size();
        }
    }
}