package com.cryptoArb.core.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionIDLoggerTest {

    @Test
    @DisplayName("Should prove each thread's ID is isolated")
    void shouldIsolateIdsPerThread() throws InterruptedException {
        // --- Given ---
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        final TransactionIDLogger idLogger = new TransactionIDLogger();

        // Latches to start and stop all threads
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(numThreads);

        // A thread-safe list to store any failures
        final ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();

        // --- When ---
        for (int i = 0; i < numThreads; i++) {
            final String threadId = "tx-id-" + i;

            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for "GO" signal

                    // 1. SET the ID for this thread
                    idLogger.setTransactionId(threadId);

                    // Add a small delay. This is to give other threads
                    // a chance to run and try to "clobber" our ID.
                    TimeUnit.MILLISECONDS.sleep(10);

                    // 2. GET the ID for this thread
                    String retrievedId = idLogger.getTransactionId();

                    // 3. CHECK if the ID is correct
                    if (retrievedId == null || !retrievedId.equals(threadId)) {
                        failures.add(
                                "Thread " + threadId + " set its ID, but retrieved: " + retrievedId
                        );
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 3. "GO!" - Release all threads
        startLatch.countDown();

        // 4. Wait for all threads to finish
        boolean finishedInTime = endLatch.await(5, TimeUnit.SECONDS);

        // --- Then ---
        executor.shutdownNow();

        // 5. Assert that no failures were recorded
        assertTrue(finishedInTime, "Test timed out");
        assertTrue(failures.isEmpty(),
                "ThreadLocal values were mixed up: " +
                        failures.stream().collect(Collectors.joining(", "))
        );
    }
}