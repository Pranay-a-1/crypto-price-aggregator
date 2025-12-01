package com.cryptoArb.crypto_price_aggregator.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe shutdown signal using volatile keyword.
 * 
 * <p>
 * <b>Educational Purpose:</b> Demonstrates volatile memory visibility
 * guarantees
 * for simple one-way state transitions (running → stopped).
 * </p>
 * 
 * <p>
 * <b>Why Volatile?</b>
 * </p>
 * <ul>
 * <li>Ensures writes are visible to all threads immediately (no CPU cache
 * delays)</li>
 * <li>Simpler than synchronized for single-variable visibility</li>
 * <li>No locking overhead for read operations</li>
 * </ul>
 * 
 * <p>
 * <b>Limitation:</b> Only suitable for single boolean flags. For complex state
 * or bidirectional transitions, use AtomicBoolean or synchronized blocks.
 * </p>
 * 
 * <p>
 * <b>Production Note:</b> This is pedagogical code. Real applications should
 * use
 * ExecutorService.shutdown() or Thread.interrupt() for proper thread lifecycle
 * management.
 * </p>
 * 
 * @author CryptoPriceAggregator Team
 * @see java.util.concurrent.atomic.AtomicBoolean
 * @see ExecutorService#shutdown()
 */
public class VolatileFlagStop {

    private static final Logger logger = LoggerFactory.getLogger(VolatileFlagStop.class);

    /**
     * The stop flag with volatile visibility guarantee.
     * 
     * <p>
     * <b>Volatile Semantics:</b>
     * </p>
     * <ul>
     * <li>All writes to this variable happen-before subsequent reads</li>
     * <li>No thread-local caching - always reads from main memory</li>
     * <li>Prevents instruction reordering around this variable</li>
     * </ul>
     */
    private volatile boolean stopped = false;

    /**
     * Checks if the stop signal has been set.
     * 
     * <p>
     * <b>Thread Safety:</b> Safe to call from multiple threads concurrently.
     * Volatile read guarantees visibility of the most recent write.
     * </p>
     * 
     * <p>
     * <b>Performance:</b> O(1) with memory barrier overhead (minimal).
     * </p>
     * 
     * @return true if stop() has been called, false otherwise
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Sets the stop signal, notifying all threads checking isStopped().
     * 
     * <p>
     * <b>Thread Safety:</b> Idempotent - safe to call multiple times.
     * Volatile write ensures all subsequent reads see the updated value.
     * </p>
     * 
     * <p>
     * <b>Design Note:</b> This is a one-way operation. Once stopped, the flag
     * cannot be reset (mirrors typical shutdown semantics).
     * </p>
     * 
     * <p>
     * <b>Performance:</b> O(1) with memory fence to flush CPU caches.
     * </p>
     */
    public void stop() {
        if (!stopped) {
            logger.debug("Stop signal set by thread: {}", Thread.currentThread().getName());
        }
        stopped = true;
    }

    /**
     * Resets the stop flag to false (for testing/reuse scenarios).
     * 
     * <p>
     * <b>Warning:</b> This method is provided for testing flexibility only.
     * Production code should typically avoid resetting shutdown flags.
     * </p>
     * 
     * <p>
     * <b>Use Case:</b> Allows test fixtures to reuse VolatileFlagStop instances
     * across multiple test methods without recreating objects.
     * </p>
     */
    public void reset() {
        logger.debug("Stop flag reset by thread: {}", Thread.currentThread().getName());
        stopped = false;
    }
}
