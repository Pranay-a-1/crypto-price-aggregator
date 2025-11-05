package com.cryptoArb.core.concurrency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

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



    @Test
    @DisplayName("Should handle concurrent producers and consumers without deadlock or lost items")
    void shouldHandleConcurrentProducersAndConsumers() throws InterruptedException {
        // --- Given ---
        final MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10); // A small capacity
        final int numProducers = 5;
        final int numConsumers = 5;
        final int itemsPerThread = 20; // 5 * 20 = 100 items total
        final int totalItems = numProducers * itemsPerThread;

        // A new thread pool for this specific, high-contention test
        ExecutorService concurrentExecutor = Executors.newFixedThreadPool(numProducers + numConsumers);

        // Latches to synchronize all threads
        // to allow us to start and stop all threads together
        // This increases contention and the chance of race conditions.
        // We want to stress-test the queue.
        // Using latches also helps avoid flaky tests.
        // (without latches, threads might start at slightly different times,
        // leading to inconsistent results)
        // startLatch.await(1) will block all threads until we call startLatch.countDown() , the 1 means we need 1 count to release all waiting threads.
        // so all threads will wait at startLatch.await() until we call startLatch.countDown() once.
        // if we had startLatch = new CountDownLatch(3) , then we would need to call startLatch.countDown() three times to release all waiting threads.
        final CountDownLatch startLatch = new CountDownLatch(1); // To start all threads
        final CountDownLatch endLatch = new CountDownLatch(numProducers + numConsumers); // To finish all tasks

        // We use AtomicInteger for thread-safe summing
        // without AtomicInteger the test could have race conditions leading to false failures.
        // the alternative is to use synchronized blocks, but AtomicInteger is simpler here.
        final AtomicInteger producedSum = new AtomicInteger(0);
        final AtomicInteger consumedSum = new AtomicInteger(0);

        // --- When ---

        // 1. Create all Producer tasks
        for (int i = 0; i < numProducers; i++) {
            final int producerId = i;
            concurrentExecutor.submit(() -> {
                try {
                    startLatch.await(); // Wait for "GO" signal
                    for (int j = 0; j < itemsPerThread; j++) {
                        // We produce a unique item (e.g., 1000 + 20*i + j)
                        int item = (producerId * itemsPerThread) + j;
                        queue.put(item);
                        producedSum.addAndGet(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown(); // Signal this producer is done
                }
            });
        }

        // 2. Create all Consumer tasks
        for (int i = 0; i < numConsumers; i++) {
            concurrentExecutor.submit(() -> {
                try {
                    startLatch.await(); // Wait for "GO" signal
                    for (int j = 0; j < itemsPerThread; j++) {
                        // This will block until an item is available
                        Integer item = queue.take();
                        consumedSum.addAndGet(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown(); // Signal this consumer is done
                }
            });
        }

        // 3. "GO!" - Release all threads at once
        startLatch.countDown();

        // 4. Wait for all 10 threads to finish, with a timeout
        boolean finishedInTime = endLatch.await(10, TimeUnit.SECONDS);

        // --- Then ---
        // Cleanup this test's executor
        concurrentExecutor.shutdownNow();

        // Assert: If we timed out, it means we had a deadlock
        assertTrue(finishedInTime, "Test timed out, indicating a deadlock or missed signal");

        // Assert: If we finished, check that no items were lost or duplicated
        assertEquals(producedSum.get(), consumedSum.get(), "The sum of produced items should equal the sum of consumed items");
        // We can also check the queue is empty, but we need a .size() method first.
        // Now we can prove the queue is empty
        assertEquals(0, queue.size(), "Queue should be empty at the end of the test");
    }
}