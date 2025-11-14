package com.cryptoArb;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A standalone main class to intentionally create memory pressure for
 * profiling, as required by Phase 10 of the project plan.
 * <p>
 * This class starts 4 threads that run in a tight loop, creating
 * millions of PriceTick objects and adding them to a shared list,
 * which will quickly fill the Java heap.
 */
public class MemoryStressTest {
    /**
    * 1. A shared list to hold all the objects.
    * This is what will fill the heap memory.
    *We wrap it in a synchronizedList to make it thread-safe,
    *as multiple threads will be writing to it.
     * <p>
     *
     * What synchronizedList does
     * Collections.synchronizedList(new ArrayList<>()) wraps an ArrayList with a single intrinsic lock.
     * Every call to add, get, size, etc., acquires that lock, making operations thread-safe for multiple writers/readers. ; so if one thread is writing to the list, no other thread can read from it.
     * When iterating, you must still do synchronized (list) { for (...) { ... } } to be safe.
     */
    private static final List<PriceTick> memoryHog =  Collections.synchronizedList(new ArrayList<>());

    // 2. We'll use a fixed pool of 4 threads to generate data.
    private static final int NUM_THREADS = 4;

    public static void main(String[] args) {
        System.out.println("Starting MemoryStressTest...");
        System.out.println("This will run indefinitely. Press Ctrl+C to stop.");
        System.out.println("Connect JVisualVM or Java Flight Recorder to this process (PID: "
                + ProcessHandle.current().pid() + ")");

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        // A helper task that creates objects in a tight loop
        Runnable objectCreationTask = () -> {
            // These are constants for our loop to create new objects
            final CurrencyPair pair = new CurrencyPair("BTC", "USD");
            final Exchange exchange = new Exchange("stress-test");
            final BigDecimal bid = new BigDecimal("50000");
            final BigDecimal ask = new BigDecimal("50001");

            // This is the "tight loop" from the project plan
            while (true) {
                // Create a new PriceTick object
                PriceTick tick = new PriceTick(
                        pair,
                        exchange,
                        Instant.now(),
                        bid,
                        ask
                );
                // Add it to the shared list
                memoryHog.add(tick);

                // Optional: Add a small sleep if the CPU usage is too high
                // try {
                //     Thread.sleep(1); // 1ms
                // } catch (InterruptedException e) {
                //     Thread.currentThread().interrupt();
                //     break;
                // }
            }
        };

        // Submit the task to be run by all threads in the pool
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(objectCreationTask);
        }

        // Keep the main thread alive (the executor threads are daemons)
        // We'll also log the heap size every 5 seconds
        try {
            while (true) {
                Thread.sleep(5000); // Wait 5 seconds
                logMemoryUsage();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Stress test interrupted.");
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Helper method to log the current heap usage.
     */
    private static void logMemoryUsage() {
        /* Get runtime
         *
         * Runtime is a class in Java that provides access to the runtime environment.
         * It is a singleton class, meaning there is only one instance of it in the JVM.
         * It provides methods to get information about the runtime environment, such as the amount of free and total memory.
         */
        Runtime runtime = Runtime.getRuntime(); // Get the runtime instance

        // Calculate memory usage
        long totalMemory = runtime.totalMemory(); // Total memory allocated to JVM ; memory here means heap memory
        long freeMemory = runtime.freeMemory();   // Free memory within the allocated heap memory
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();     // Max memory JVM can request

        System.out.printf("Used Memory: %d MB | Total Memory: %d MB | Max Memory: %d MB%n",
                usedMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024));
    }
}