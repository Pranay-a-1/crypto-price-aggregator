package com.cryptoArb.crypto_price_aggregator.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Thread-safe bounded blocking queue using manual synchronization.
 * 
 * <p>
 * <b>Educational Purpose:</b> Demonstrates the producer-consumer pattern with
 * synchronized, wait(), and notify() before introducing java.util.concurrent
 * abstractions.
 * </p>
 * 
 * <p>
 * <b>Key Synchronization Mechanisms:</b>
 * </p>
 * <ul>
 * <li><b>synchronized:</b> Mutual exclusion for put/take operations</li>
 * <li><b>wait():</b> Releases lock and blocks until notified</li>
 * <li><b>notifyAll():</b> Wakes all waiting threads to recheck conditions</li>
 * <li><b>while (condition):</b> Guards against spurious wakeups</li>
 * </ul>
 * 
 * <p>
 * <b>Why Manual Implementation?</b>
 * </p>
 * <ul>
 * <li>Understand low-level concurrency primitives</li>
 * <li>Appreciate java.util.concurrent.BlockingQueue design</li>
 * <li>Debug common pitfalls (deadlocks, lost notifications)</li>
 * </ul>
 * 
 * <p>
 * <b>Production Alternative:</b> Use
 * {@link java.util.concurrent.ArrayBlockingQueue}
 * which is optimized with separate notEmpty/notFull conditions and lock
 * splitting.
 * </p>
 * 
 * <p>
 * <b>Limitations:</b>
 * </p>
 * <ul>
 * <li>Single lock = contention between producers and consumers</li>
 * <li>notifyAll() wakes all threads (thundering herd problem)</li>
 * <li>No timeout support for bounded blocking</li>
 * </ul>
 * 
 * @param <T> Type of elements held in this queue
 * @author CryptoPriceAggregator Team
 * @see java.util.concurrent.ArrayBlockingQueue
 * @see java.util.concurrent.locks.Condition
 */
public class MyBlockingQueue<T> {

    private static final Logger logger = LoggerFactory.getLogger(MyBlockingQueue.class);

    /**
     * Internal FIFO queue for element storage.
     * Using LinkedList for O(1) addLast/removeFirst operations.
     */
    private final Queue<T> queue;

    /**
     * Maximum number of elements this queue can hold.
     * Enforced by blocking put() when queue is full.
     */
    private final int capacity;

    /**
     * Creates a bounded blocking queue with the specified capacity.
     * 
     * @param capacity Maximum number of elements (must be > 0)
     * @throws IllegalArgumentException if capacity <= 0
     */
    public MyBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacity);
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();

        logger.debug("MyBlockingQueue created with capacity: {}", capacity);
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary for
     * space.
     * 
     * <p>
     * <b>Blocking Behavior:</b> If queue is full, this method blocks until:
     * <ul>
     * <li>Another thread calls take() to free space, OR</li>
     * <li>The current thread is interrupted</li>
     * </ul>
     * 
     * <p>
     * <b>Thread Safety:</b> Uses synchronized block for mutual exclusion.
     * Multiple producers can safely call put() concurrently.
     * </p>
     * 
     * <p>
     * <b>Why notifyAll() instead of notify()?</b> With mixed producers/consumers,
     * notify() might wake a producer when we need to wake a consumer (or vice
     * versa).
     * notifyAll() is safer but less efficient (wakes all waiters).
     * </p>
     * 
     * <p>
     * <b>Time Complexity:</b>
     * </p>
     * <ul>
     * <li>Best case: O(1) - queue not full, immediate add</li>
     * <li>Worst case: O(∞) - blocks until space available</li>
     * <li>Amortized: O(1) assuming balanced produce/consume rates</li>
     * </ul>
     * 
     * @param element The element to add (must not be null)
     * @throws InterruptedException if interrupted while waiting for space
     * @throws NullPointerException if element is null
     */
    public void put(T element) throws InterruptedException {
        if (element == null) {
            throw new NullPointerException("Cannot add null elements to queue");
        }

        synchronized (this) {
            // Wait while queue is full (use while to handle spurious wakeups)
            while (queue.size() >= capacity) {
                logger.trace("Queue full ({}), thread {} waiting to put",
                        capacity, Thread.currentThread().getName());

                // Release lock and wait for notification
                // This is atomic: releases lock THEN waits (no race condition)
                wait();

                // When woken, recheck condition (spurious wakeup protection)
            }

            // Queue has space - add element
            queue.add(element);
            logger.trace("Thread {} added element, queue size now: {}",
                    Thread.currentThread().getName(), queue.size());

            // Notify waiting consumers (queue is no longer empty)
            // Using notifyAll() to wake all waiting threads
            notifyAll();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary for an
     * element.
     * 
     * <p>
     * <b>Blocking Behavior:</b> If queue is empty, this method blocks until:
     * <ul>
     * <li>Another thread calls put() to add an element, OR</li>
     * <li>The current thread is interrupted</li>
     * </ul>
     * 
     * <p>
     * <b>Thread Safety:</b> Uses synchronized block for mutual exclusion.
     * Multiple consumers can safely call take() concurrently.
     * </p>
     * 
     * <p>
     * <b>FIFO Ordering:</b> Elements are returned in the order they were added.
     * Uses LinkedList.poll() which removes from the head.
     * </p>
     * 
     * <p>
     * <b>Time Complexity:</b>
     * </p>
     * <ul>
     * <li>Best case: O(1) - queue not empty, immediate removal</li>
     * <li>Worst case: O(∞) - blocks until element available</li>
     * <li>Amortized: O(1) assuming balanced produce/consume rates</li>
     * </ul>
     * 
     * @return The head element of this queue
     * @throws InterruptedException if interrupted while waiting for an element
     */
    public T take() throws InterruptedException {
        synchronized (this) {
            // Wait while queue is empty (use while to handle spurious wakeups)
            while (queue.isEmpty()) {
                logger.trace("Queue empty, thread {} waiting to take",
                        Thread.currentThread().getName());

                // Release lock and wait for notification
                wait();

                // When woken, recheck condition
            }

            // Queue has elements - remove and return head
            T element = queue.poll();
            logger.trace("Thread {} removed element, queue size now: {}",
                    Thread.currentThread().getName(), queue.size());

            // Notify waiting producers (queue is no longer full)
            notifyAll();

            return element;
        }
    }

    /**
     * Returns the number of elements currently in this queue.
     * 
     * <p>
     * <b>Thread Safety:</b> Synchronized for consistent snapshot of size.
     * Note that by the time this method returns, the size may have changed
     * due to concurrent operations.
     * </p>
     * 
     * <p>
     * <b>Time Complexity:</b> O(1) - LinkedList maintains size counter.
     * </p>
     * 
     * @return The current number of elements
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * Returns true if this queue contains no elements.
     * 
     * <p>
     * <b>Thread Safety:</b> Synchronized for visibility guarantees.
     * Result may be stale immediately after returning due to concurrent operations.
     * </p>
     * 
     * <p>
     * <b>Use Case:</b> Primarily for testing and debugging.
     * Production code should use blocking take() instead of polling isEmpty().
     * </p>
     * 
     * @return true if queue is empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns true if this queue is at capacity.
     * 
     * <p>
     * <b>Thread Safety:</b> Synchronized for visibility guarantees.
     * Result may be stale immediately after returning due to concurrent operations.
     * </p>
     * 
     * <p>
     * <b>Use Case:</b> Primarily for testing and flow control metrics.
     * Production code should use blocking put() instead of checking isFull().
     * </p>
     * 
     * @return true if queue is full, false otherwise
     */
    public synchronized boolean isFull() {
        return queue.size() >= capacity;
    }

    /**
     * Returns the maximum capacity of this queue.
     * 
     * <p>
     * <b>Immutable Property:</b> Capacity is set at construction and never changes.
     * </p>
     * 
     * @return The maximum number of elements this queue can hold
     */
    public int getCapacity() {
        return capacity;
    }
}
