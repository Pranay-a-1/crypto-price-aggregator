package com.cryptoArb.core.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;



class VolatileFlagStopTest {

    @Test
    @DisplayName("Should stop the worker thread when stop() is called from another thread")
    void shouldStopWorkerWhenStopIsCalled() throws InterruptedException {
        // Given: A new worker task and a thread to run it
        VolatileFlagStop workerTask = new VolatileFlagStop();
        Thread workerThread = new Thread(workerTask);

        // When: We start the worker and then call stop from this (main) thread
        workerThread.start();

        // Give the worker a moment to enter its loop
        TimeUnit.MILLISECONDS.sleep(50);

        // Signal the worker to stop
        workerTask.stop();

        // Then: The worker thread should terminate gracefully
        // We give it 1 second to stop. If it doesn't, join() times out
        // and isAlive() will be true.
        workerThread.join(1000);

        assertFalse(workerThread.isAlive(), "Worker thread did not stop within 1 second.");
    }
}