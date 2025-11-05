package com.cryptoArb.core.concurrency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MyBlockingQueueTest {

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        // We use a single-thread executor to run our consumer
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        // We must shut down the executor to stop the test
        // .shutdownNow() will interrupt any waiting threads
        executor.shutdownNow();
    }

    @Test
    @DisplayName("Should block (wait) when taking from an empty queue")
    void shouldBlockWhenTakingFromEmptyQueue() throws Exception {
        // Given: An empty MyBlockingQueue
        // This line will fail to compile (RED)
        final MyBlockingQueue<Integer> queue = new MyBlockingQueue<>();

        // When: We submit a task (a consumer) that tries to .take()
        Future<?> consumerTask = executor.submit(() -> {
            try {
                // This call should block forever
                queue.take();
            } catch (InterruptedException e) {
                // This is expected when the test shuts down
                Thread.currentThread().interrupt();
            }
        });

        // Then: We give the consumer thread a moment to start and block
        TimeUnit.MILLISECONDS.sleep(200); // Wait for the .take() to be called

        // We get the underlying thread from the task
        // (Note: This is a test-specific trick; not for production)
        // We need to find the thread running our task.
        // A real test might use Awaitility, but for now, we just
        // check that the task is not "done".

        // Let's use a simpler assertion for now:
        // If the task finishes, it means .take() returned, which is a failure.
        boolean isDone = consumerTask.isDone();

        // Assert: The task should NOT be done, because it should be waiting.
        assertFalse(isDone, "Task should be blocked, but it finished.");
    }
}