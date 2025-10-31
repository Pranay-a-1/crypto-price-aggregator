package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A service responsible for safely collecting
 * arbitrage opportunities from concurrent threads.
 *
 * VERSION 3: Refactored to ReentrantLock (STILL GREEN)
 *
 *
 * This code is functionally identical to our synchronized version, so our OpportunityAggregatorTest will still pass. We are still in the "Green" state.
 *
 * This ReentrantLock is great, but it has one "problem": it's a single lock.
 * If 100 threads are trying to read the list (using getOpportunityCount) and 1 thread is trying to write (using addOpportunity), all 101 threads have to wait in a single line.
 *
 * This isn't very efficient. The plan has a solution for this, mentioning a ReadWriteLock.
 */
public class OpportunityAggregator {

    private final List<ArbitrageOpportunity> opportunities = new ArrayList<>();

    // 1. Create the lock as a private final field
    private final Lock lock = new ReentrantLock();

    /**
     * Adds a found opportunity to the list.
     * Thread-safe using ReentrantLock.
     */
    public void addOpportunity(ArbitrageOpportunity opportunity) {
        // 2. Acquire the lock before touching the list
        lock.lock();
        try {
            // 3. This is our "critical section"
            this.opportunities.add(opportunity);
        } finally {
            // 4. Release the lock in a 'finally' block
            // This guarantees it unlocks even if .add() fails
            lock.unlock();
        }
    }

    /**
     * Returns a snapshot of all found opportunities.
     * Thread-safe using ReentrantLock.
     */
    public List<ArbitrageOpportunity> getOpportunities() {
        lock.lock();
        try {
            return new ArrayList<>(this.opportunities);
        } finally {
            lock.unlock();
        }
    }

    public int getOpportunityCount() {
        lock.lock();
        try {
            return this.opportunities.size();
        } finally {
            lock.unlock();
        }
    }
}