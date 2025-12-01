# ManualConcurrentPriceEngine Design Explanation

## Explain the Why

### Why `Executors.newFixedThreadPool`?

- **Control**: Unlike `newCachedThreadPool` (which creates unlimited threads under load), a fixed pool prevents resource exhaustion.
- **Reuse**: Threads are reused for subsequent tasks, reducing the overhead of thread creation.

### Why `invokeAll` instead of a loop of `submit()`?

- **Simplicity**: `invokeAll` handles the collection of futures and waits for all tasks to finish (or the timeout to expire, though we didn't set one yet). It simplifies the "scatter-gather" pattern significantly compared to managing a latch or iterating futures manually.

### Why `catch (ExecutionException e)` inside the loop?

- **Resilience**: We want **Partial Success**. If Binance is down but Coinbase is up, we still want to show the Coinbase price. If we let the exception propagate, one failure would crash the entire aggregation request.

## Potential Reviewer/Interviewer Questions

### Q: You initialize the ExecutorService in the constructor. When is it shut down?

**A:** In this implementation, it isn't, which is a resource leak! This is intentional for this phase to demonstrate the "pain" of manual management. In a real Spring app, we would implement `DisposableBean` to call `executorService.shutdown()` on context destruction, or let Spring manage the pool via `@Async`.

### Q: What happens if one task hangs forever?

**A:** Currently, `invokeAll(tasks)` without a timeout will block indefinitely. This could starve the thread pool. A production-ready version should use `invokeAll(tasks, timeout, unit)` to enforce an SLA (Service Level Agreement).

### Q: Is ArrayList for results thread-safe?

**A:** Yes, in this specific context. We are adding to results sequentially in the main thread after `invokeAll` returns. We are not adding to it from inside the concurrent tasks, so no synchronization is needed for the list itself.

