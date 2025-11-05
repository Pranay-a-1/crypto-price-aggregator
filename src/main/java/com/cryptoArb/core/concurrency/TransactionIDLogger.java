package com.cryptoArb.core.concurrency;

/**
 * Demonstrates using ThreadLocal to store a value
 * that is isolated to a specific thread.
 *
 * This fulfills Phase 7 of the project plan.
 */
public class TransactionIDLogger {

    // 1. The ThreadLocal variable.
    // It is 'private' and 'final'.
    // It is 'static' because the ThreadLocal storage is tied
    // to the thread itself, not to any specific instance of this class.
    // This way, multiple logger instances can all access the *same*
    // transaction ID for the *current* thread.
    private static final ThreadLocal<String> transactionId = new ThreadLocal<>();

    /**
     * Sets the transaction ID for the currently executing thread.
     * This value will ONLY be visible to this thread.
     *
     * @param id The transaction ID to store.
     */
    public void setTransactionId(String id) {
        // 2. Call .set() to store the value in this thread's private map
        transactionId.set(id);
    }

    /**
     * Gets the transaction ID for the currently executing thread.
     *
     * @return The ID, or null if it was never set for this thread.
     */
    public String getTransactionId() {
        // 3. Call .get() to retrieve this thread's private value
        return transactionId.get();
    }


    /**
     * Clears the transaction ID from the currently executing thread.
     * This is critical to prevent memory leaks in thread pools.
     */
    public void clear() {
        // 4. Call .remove() to delete this thread's private value
        transactionId.remove();
    }
}