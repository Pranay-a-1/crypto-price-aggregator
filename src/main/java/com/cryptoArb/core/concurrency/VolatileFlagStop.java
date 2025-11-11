package com.cryptoArb.core.concurrency;



/**
 * Demonstrates using a 'volatile' flag for inter-thread communication.
 * This class implements Runnable so it can be executed by a Thread.
 *
 * This class is created to showcase the use of a 'volatile' boolean flag
 * to signal a running thread to stop its execution from another thread.
 *
 * Usage:
 * 1. Create an instance of VolatileFlagStop.
 * 2. Start it in a new Thread.
 * 3. Call the stop() method from another thread to signal it to stop.
 *
 *
 */
public class VolatileFlagStop implements Runnable {

    // 1. The flag is 'volatile'
    // This ensures that changes made by one thread (the main test thread)
    // are immediately visible to the other thread (the worker thread).
    private volatile boolean running = true;

    /**
     * The main work loop of the thread.
     * It will continuously loop as long as 'running' is true
     */
    @Override
    public void run() {
        // Keep looping while the flag is true
        while (this.running) {
            // In a real app, we might do work here.
            // For this test, we just loop.
            // We can add a small printout to see it running (optional).
            System.out.println("Worker is running...");
        }
        // When the loop exits, the run() method finishes and the thread terminates.
        System.out.println("Worker thread stopping.");
    }

    /**
     * Signals the worker thread to stop.
     * This method is called from a different thread.
     */
    public void stop() {
        System.out.println("Stop signal received.");
        this.running = false;
    }
}