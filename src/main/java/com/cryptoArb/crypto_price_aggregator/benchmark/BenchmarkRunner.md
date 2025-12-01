# BenchmarkRunner Explanation

## Phase 1: Red Implementation - Explain the Why

### Why create a new Engine instance inside `runBenchmark`?

**Isolation:** Benchmarks should run in a clean environment. Using a shared engine might introduce interference from other running tasks or thread pool states. Creating a new one ensures we measure exactly the cost of spinning up the task (including thread pool creation overhead, if we included that, though here `Executors` is relatively fast).

### Why a record for the result?

**Data Carrier:** The result is a simple immutable data carrier. Java Records (available in Java 17, which we are using) are perfect for this, reducing boilerplate code.

### Why `System.nanoTime()`?

**Precision:** As discussed in the Red phase, `nanoTime()` provides high-resolution, monotonic time suitable for measuring elapsed duration, unlike `currentTimeMillis()` which is wall-clock time.

### Potential Reviewer/Interviewer Questions

**Q: Does this benchmark account for JVM Warmup?**

A: No. This is a "Cold" benchmark. In a real performance analysis, we would run the code thousands of times to let the JIT (Just-In-Time) compiler optimize the hot paths. This benchmark purely demonstrates the architectural difference (IO blocking) which is significant enough to show even without JIT warmup.

**Q: Why catch generic `Exception` in `runSequential`?**

A: In a benchmark, we care about the mechanism's timing. If a fetcher throws an exception, we still paid the "cost" of calling it and waiting for the exception. We catch it to ensure the loop completes all iterations so the timing comparison is fair (assuming parallel also handles exceptions, which our `Engine` does).

**Q: Is `@Component` appropriate here?**

A: Yes, assuming we might want to inject this runner into a Controller or a `CommandLineRunner` later to trigger benchmarks from the application context.

---

## Phase 2: Green Implementation - Explain the Why

### Why multiple iterations?

Single runs are susceptible to outliers (e.g., CPU scaling, GC pauses). Averaging 5-10 runs gives a much more reliable indicator of typical performance.

### Why Standard Deviation?

Mean tells us the "center", but StdDev tells us the "consistency". If Parallel execution has a high StdDev, it implies the threading overhead or context switching is unpredictable, which is a useful signal for debugging concurrency issues.

### Why `LongSummaryStatistics`?

It's a standard Java stream collector that efficiently computes count, sum, min, average, and max in one pass (mostly). It simplifies the stats calculation.

### Potential Reviewer/Interviewer Questions

**Q: Why did you reuse the engine instance across iterations this time?**

A: In the "Green" phase, I created a new engine per run. In this Refactor, I reused it to simulate a "warm" thread pool. Thread creation is expensive; usually, we want to measure the task execution time on an active system, not the startup cost every time.

**Q: Is `Math.pow(t - mean, 2)` efficient inside a stream?**

A: For a benchmark with N=5 or N=10, it's negligible. For massive datasets, we might use a running variance algorithm (Welford's Algorithm) to avoid iterating twice (once for mean, once for variance). For this purpose, readability > micro-optimization.

**Q: How would you handle Warmup vs. Measurement iterations?**

A: Ideally, I'd run loop k times and discard the results (Warmup), then run n times for measurement. I omitted explicit warmup logic here to keep the code concise (KISS), but it's a standard practice in tools like JMH.