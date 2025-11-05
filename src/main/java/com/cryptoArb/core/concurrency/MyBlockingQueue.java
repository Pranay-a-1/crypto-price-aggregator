package com.cryptoArb.core.concurrency;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A from-scratch implementation of a simple BlockingQueue to demonstrate
 * the use of wait() and notifyAll().
 *
 * This fulfills Phase 7 of the project plan.
 */
public class MyBlockingQueue<T> {

    // 1. The internal data structure to hold items
    // We'll use a simple Queue (FIFO) for this example.
    // In a real implementation, we might want to add capacity limits, etc.
    // Other alternatives could be ArrayDeque, but LinkedList suffices for this demo.
    // Note: This queue is not thread-safe by itself; we will manage synchronization.
    // Other alternatives are : ConcurrentLinkedQueue (non-blocking) or ArrayBlockingQueue (bounded blocking)
    // and also other Queue implementations such as PriorityQueue, Deque, etc.
    private final Queue<T> queue = new LinkedList<>(); // Using LinkedList as a simple queue

    // 2. The intrinsic lock object
    // We create a dedicated, private 'lock' object. This is a best
    // practice over locking 'this' or the 'queue' itself, as it
    // prevents external code from interfering with our lock.
    // we use Object because it has the methods we need: wait(), notify(), notifyAll()
    // Other alternatives could be using ReentrantLock from java.util.concurrent.locks
    private final Object lock = new Object();

    // --- NEW: Add capacity field ---
    private final int capacity;

    /**
     * Creates a new blocking queue with a (virtually) unbounded capacity.
     */
    public MyBlockingQueue() {
        // Call our other constructor with a default "unbounded" size
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a new blocking queue with a fixed capacity.
     * @param capacity The maximum number of items the queue can hold.
     */
    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Takes an item from the queue.
     * If the queue is empty, this method will block (wait)
     * until an item is available.
     *
     * @return The item from the front of the queue
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public T take() throws InterruptedException {
        // 3. Acquire the lock
        /*
        *In Java, every single object (literally any object you create) comes with its own built-in, invisible lock. This is often called an intrinsic lock or monitor.
        *It's like every object is built with its own private, one-key "VIP room."
        *When you write synchronized (someObject), you are just saying, "I want to use this specific object's built-in lock."
        *Your private final Object lock = new Object(); line? You literally just created an object for the sole purpose of using its built-in lock.
         */
        synchronized (lock) {
            // 4. Check if the queue is empty
            // --- MODIFIED ---
            // We MUST use a 'while' loop instead of an 'if'.
            // This is the standard, correct pattern to guard against
            // "spurious wakeups" (where a thread can wake up
            // even if notify() wasn't called).
            while (queue.isEmpty()) { //if (queue.isEmpty()) {
                // 5. If empty, release the lock and wait
                // This call causes the consumer thread to pause
                // and enter the 'WAITING' state.
                lock.wait();
            }


            // --- MODIFIED ---
            // We are about to remove an item, so the queue is no longer full.
            // We MUST notify any waiting PRODUCER threads.
            T item = queue.remove();
            lock.notifyAll(); // Wake up any waiting producers
            return item;
        }
    }

    // --- NEW METHOD ---
    /**
     * Puts an item into the queue.
     * This method will also notify any threads that are
     * waiting for an item to become available.
     *
     * @param item The item to add to the queue.
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void put(T item) throws InterruptedException {
        synchronized (lock) {
            // --- MODIFIED ---
            // Wait while the queue is full (at capacity)
            while (queue.size() == capacity) {
                lock.wait();
            }
            // 1. Add the item to the queue
            queue.add(item);

            // 2. Notify ALL waiting threads (i.e., any consumers threads
            //    stuck in take()) that the state has changed.
            //    This is the "signal" our test is waiting for.
            lock.notifyAll();
        }
    }
}
