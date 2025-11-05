package com.cryptoArb.core.concurrency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
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


    @Test
    @DisplayName("Should unblock take() when put() is called")
    void shouldPutAndUnblockTake() throws Exception {
        // Given: An empty MyBlockingQueue
        final MyBlockingQueue<Integer> queue = new MyBlockingQueue<>();
        final Integer testValue = 42;

        // When: We submit a consumer task (as a Callable)
        // This task will block, then return the value it receives
        Future<Integer> consumerTask = executor.submit(() -> {
            try {
                // This call will block until put() notifies it
                return queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null; // Should not happen in this test
            }
        });

        // Give the consumer a moment to start and enter the wait() state
        TimeUnit.MILLISECONDS.sleep(200);

        // --- MODIFIED ---
        // We must now handle the InterruptedException
        try {
            // Now, the producer (our main test thread) puts an item
            queue.put(testValue);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        // Then: The consumer task should complete
        // We use .get() which waits for the Future to have a result.
        // We give it a 1-second timeout.
        // without timeout, the test could hang indefinitely if something goes wrong.
        Integer returnedValue = consumerTask.get(1, TimeUnit.SECONDS);

        // Assert: The value taken is the value we put
        assertEquals(testValue, returnedValue, "The value taken should be the value that was put");
    }


    @Test
    @DisplayName("Should block (wait) when putting into a full queue")
    void shouldBlockWhenPuttingToFullQueue() throws Exception {
        // Given: A queue with a capacity of 1
        // This line will fail to compile (RED)
        final MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(1);


        // --- MODIFIED ---
        // This call to put() must also be handled
        try {
            // And the queue is full
            // puts an item to fill the queue
            queue.put(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        // When: We submit a task (a producer) that tries to .put()
        Future<?> producerTask = executor.submit(() -> {
            try {
                // This call should block forever
                queue.put(2);
            } catch (InterruptedException e) {
                // This is expected when the test shuts down
                Thread.currentThread().interrupt();
            }
        });

        // Then: We give the producer thread a moment to start and block
        TimeUnit.MILLISECONDS.sleep(200);

        // Assert: The task should NOT be done, because it should be waiting.
        boolean isDone = producerTask.isDone(); // Check if the task has completed
        assertFalse(isDone, "Task should be blocked, but it finished.");
    }
}