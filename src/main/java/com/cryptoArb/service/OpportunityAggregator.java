package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A service responsible for safely collecting
 * arbitrage opportunities from concurrent threads.
 *
 * VERSION 4: Refactored to ReadWriteLock (STILL GREEN)
 */
public class OpportunityAggregator {

    private final List<ArbitrageOpportunity> opportunities = new ArrayList<>();

    // 1. Create ReadWriteLock
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // 2. Get the specific read and write locks from it
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();


    /**
     * Adds a found opportunity to the list.
     * This is a WRITE operation, so we use the writeLock.
     * It is EXCLUSIVE.
     */
    public void addOpportunity(ArbitrageOpportunity opportunity) {
        // Use the write lock
        writeLock.lock();
        try {
            this.opportunities.add(opportunity);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns a snapshot of all found opportunities.
     * This is a READ operation, so we use the readLock.
     * It is SHARED.
     */
    public List<ArbitrageOpportunity> getOpportunities() {
        // Use the read lock
        readLock.lock();
        try {
            return new ArrayList<>(this.opportunities);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * This is also a READ operation.
     */
    public int getOpportunityCount() {
        readLock.lock();
        try {
            return this.opportunities.size();
        } finally {
            readLock.unlock();
        }
    }
}