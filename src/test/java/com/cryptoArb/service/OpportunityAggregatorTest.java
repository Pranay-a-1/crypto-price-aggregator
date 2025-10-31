package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;
import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OpportunityAggregatorTest {


    private OpportunityAggregator opportunityAggregator;

    // Helper method to create a fake opportunity
    private ArbitrageOpportunity createDummyOpportunity() {
        return new ArbitrageOpportunity(
                new CurrencyPair("BTC", "USD"),
                Instant.now(),
                new Exchange("buy-exchange"),
                BigDecimal.valueOf(100),
                new Exchange("sell-exchange"),
                BigDecimal.valueOf(101)
        );
    }


    @Test
    @DisplayName("Should safely add 1000 opportunities from 100 threads")
    void testConcurrentAdditions() throws InterruptedException {
        // --- Arrange ---
        // number of threads is 100 and less than 1000 tasks because each thread can add multiple tasks
        int numThreads = 100; // Number of concurrent threads
        int numTasks = 1000; // Total number of opportunities to add


        // Create a fixed-size thread pool to run concurrent tasks.
        // Bounds concurrency to `numThreads`, reuses threads for efficiency,
        // and gives predictable parallelism for this test. Remember to shut down the executor after use.
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        // CountDownLatch to make all threads start at the same time
        CountDownLatch startLatch = new CountDownLatch(1); // 1 latch to start all threads, meaning all threads wait for this latch to reach zero before starting
        // CountDownLatch to wait for all tasks to finish
        CountDownLatch endLatch = new CountDownLatch(numTasks); // numTasks latches to wait for all tasks to finish

        opportunityAggregator = new OpportunityAggregator();



        // --- Act ---
        // Create 1000 tasks and submit them
        for (int i = 0; i < numTasks; i++) {
            executor.submit(() -> {
                try {
                    // Wait for the "go" signal
                    startLatch.await();
                    // --- This is the action under test ---
                    opportunityAggregator.addOpportunity(createDummyOpportunity());
                    // ------------------------------------
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }



        // Give a moment for all threads to line up at the latch
        Thread.sleep(100);
        // "GO!" - Release the latch, all threads run at once
        startLatch.countDown();


        // Wait for all tasks to complete, with a timeout
        boolean finishedInTime = endLatch.await(10, TimeUnit.SECONDS);


        // --- ASSERT ---
        assertTrue(finishedInTime, "Tasks did not complete in time");

        // This is the assertion that will fail
        // We expect 1000, but due to race conditions, we will get < 1000
        assertEquals(numTasks, opportunityAggregator.getOpportunityCount(),
                "Lost writes due to race condition");
    }


    @Test
    @DisplayName("Should demonstrate race condition with simple thread pool without using ExecutorService and CountDownLatch")
    void testSimpleConcurrentAdditions() throws InterruptedException {
        // --- Arrange ---
        int numThreads = 10; // Using fewer threads for clarity
        int tasksPerThread = 100; // Each thread will add 100 opportunities meaning total 1000 tasks
        int totalExpectedTasks = numThreads * tasksPerThread; // 1000 total tasks

        opportunityAggregator = new OpportunityAggregator();
        Thread[] threads = new Thread[numThreads]; // Array to hold thread references

        // --- Act ---
        // Create and start threads
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                // Each thread adds multiple opportunities
                for (int j = 0; j < tasksPerThread; j++) {
                    opportunityAggregator.addOpportunity(createDummyOpportunity());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // --- Assert ---
        // Due to race condition, actual count will likely be less than expected
        assertEquals(totalExpectedTasks, opportunityAggregator.getOpportunityCount(),
                "Lost writes due to race condition");
    }



}