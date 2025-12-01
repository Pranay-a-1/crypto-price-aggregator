package com.cryptoArb.crypto_price_aggregator.concurrency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for VolatileFlagStop - demonstrates volatile visibility guarantees.
 * 
 * Learning Objectives:
 * - Understand volatile keyword for cross-thread visibility
 * - Test thread-safe shutdown signaling
 * - Verify multiple threads can read shared flag without synchronization
 */
@DisplayName("VolatileFlagStop - Thread-Safe Shutdown Signal Tests")
class VolatileFlagStopTest {

    @Test
    @DisplayName("Given multiple reader threads, when stop flag is set, then all threads should see the change")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldAllowMultipleThreadsToReadStopFlag() throws InterruptedException {
        // GIVEN: A volatile flag stop instance
        VolatileFlagStop flagStop = new VolatileFlagStop();

        // Track how many threads successfully detected the stop signal
        int numReaderThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numReaderThreads);
        List<Boolean> threadResults = new ArrayList<>();

        // WHEN: Multiple threads continuously check the flag
        for (int i = 0; i < numReaderThreads; i++) {
            Thread readerThread = new Thread(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // Spin until stop flag is set
                    while (!flagStop.isStopped()) {
                        // Busy-wait to test volatile visibility
                        Thread.yield(); // Hint to scheduler to allow other threads
                    }

                    // Flag was detected as stopped
                    synchronized (threadResults) {
                        threadResults.add(true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    synchronized (threadResults) {
                        threadResults.add(false);
                    }
                } finally {
                    completionLatch.countDown();
                }
            }, "Reader-" + i);
            readerThread.start();
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Give threads time to start spinning
        Thread.sleep(100);

        // Set the stop flag
        flagStop.stop();

        // THEN: All threads should detect the change and complete
        boolean allThreadsCompleted = completionLatch.await(3, TimeUnit.SECONDS);
        assertTrue(allThreadsCompleted,
                "All reader threads should have detected the stop flag within timeout");

        assertEquals(numReaderThreads, threadResults.size(),
                "All threads should have recorded their results");

        assertTrue(threadResults.stream().allMatch(result -> result),
                "All threads should have successfully detected the stop flag");
    }

    @Test
    @DisplayName("Given flag not stopped, when isStopped called, then should return false")
    void shouldReturnFalseWhenNotStopped() {
        // GIVEN
        VolatileFlagStop flagStop = new VolatileFlagStop();

        // WHEN & THEN
        assertFalse(flagStop.isStopped(), "Initially, flag should not be stopped");
    }

    @Test
    @DisplayName("Given flag stopped, when isStopped called, then should return true")
    void shouldReturnTrueWhenStopped() {
        // GIVEN
        VolatileFlagStop flagStop = new VolatileFlagStop();

        // WHEN
        flagStop.stop();

        // THEN
        assertTrue(flagStop.isStopped(), "After calling stop(), flag should be stopped");
    }

    @Test
    @DisplayName("Given flag stopped, when stop called again, then should remain stopped (idempotent)")
    void shouldBeIdempotent() {
        // GIVEN
        VolatileFlagStop flagStop = new VolatileFlagStop();
        flagStop.stop();

        // WHEN
        flagStop.stop(); // Call again

        // THEN
        assertTrue(flagStop.isStopped(), "Calling stop() multiple times should be safe");
    }
}
