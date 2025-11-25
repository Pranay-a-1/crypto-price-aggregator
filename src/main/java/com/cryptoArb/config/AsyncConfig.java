package com.cryptoArb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Phase 14: Spring Async Configuration.
 * 
 * Enables Spring's @Async support and configures a custom thread pool executor
 * for asynchronous method execution. This demonstrates Spring's concurrency
 * abstractions
 * and allows us to control thread pool behavior for async operations.
 * 
 * Design Decisions:
 * - Separate thread pool from web container threads (isolation)
 * - Named threads for debugging/profiling
 * - Bounded queue to prevent memory exhaustion
 * - CallerRunsPolicy for graceful degradation under load
 * 
 * Configuration Values:
 * - Core Pool Size: 5 threads (initial pool size)
 * - Max Pool Size: 10 threads (maximum under load)
 * - Queue Capacity: 100 tasks (pending task buffer)
 * - Thread Prefix: "async-report-" (for log filtering/debugging)
 * 
 * Why These Values?
 * - Small enough for local development/testing
 * - Large enough to demonstrate concurrent execution
 * - Can be externalized to application.properties in production
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Defines the task executor for @Async methods.
     * 
     * Thread Pool Sizing Strategy:
     * - Core Size (5): Number of threads to keep alive even when idle
     * - Max Size (10): Maximum threads when queue is full
     * - Queue Capacity (100): Tasks waiting when all core threads are busy
     * 
     * Flow:
     * 1. Tasks 1-5: Executed immediately on core threads
     * 2. Tasks 6-105: Queued (queue capacity = 100)
     * 3. Tasks 106-110: New threads created up to max size (10)
     * 4. Task 111+: CallerRunsPolicy executes task on caller's thread
     * (backpressure)
     * 
     * Rejection Policy (CallerRunsPolicy):
     * - Provides automatic throttling when system is overwhelmed
     * - Prevents task loss (unlike AbortPolicy)
     * - Applies backpressure to producers (slows down task submission)
     * 
     * @return Configured ThreadPoolTaskExecutor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Initializing custom ThreadPoolTaskExecutor for @Async methods");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: threads to keep alive even when idle
        executor.setCorePoolSize(5);
        log.debug("Set core pool size: 5");

        // Max pool size: maximum threads when queue is full
        executor.setMaxPoolSize(10);
        log.debug("Set max pool size: 10");

        // Queue capacity: tasks waiting when all core threads busy
        executor.setQueueCapacity(100);
        log.debug("Set queue capacity: 100");

        // Thread name prefix: makes debugging easier
        executor.setThreadNamePrefix("async-report-");
        log.debug("Set thread name prefix: async-report-");

        // Rejection policy: what happens when queue is full and max threads reached
        // CallerRunsPolicy: task executes on caller's thread (graceful degradation)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        log.debug("Set rejection policy: CallerRunsPolicy");

        // Wait for tasks to complete on shutdown (graceful shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        log.debug("Configured graceful shutdown with 60s timeout");

        executor.initialize();
        log.info("ThreadPoolTaskExecutor initialized successfully");

        return executor;
    }

    /*
     * Alternative Configuration Approaches:
     * 
     * 1. Simple Executor (Not Recommended):
     * return Executors.newFixedThreadPool(10);
     * - Pros: One line of code
     * - Cons: Unbounded queue (memory leak risk), no thread naming, no metrics
     * 
     * 2. ForkJoinPool (Alternative for CPU-Intensive):
     * return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
     * - Pros: Better for divide-and-conquer CPU tasks
     * - Cons: Not ideal for I/O-bound tasks (like database queries)
     * 
     * 3. Virtual Threads (Java 21+):
     * return Executors.newVirtualThreadPerTaskExecutor();
     * - Pros: Millions of lightweight threads, no pool sizing needed
     * - Cons: Requires Java 21+, Phase 14 uses Java 17
     * 
     * Current choice: ThreadPoolTaskExecutor provides best balance of control,
     * observability, and Spring integration for Java 17.
     */
}
