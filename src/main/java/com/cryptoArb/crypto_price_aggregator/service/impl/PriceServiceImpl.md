# PriceServiceImpl Implementation Details

## Explain the Why

### Why instantiate ManualConcurrentPriceEngine inside the constructor?

**Evolutionary Design:** We are explicitly showing the addition of a manual concurrency component. In a "Professional Refactor" (later phases), we would define the Engine as a Spring Bean (`@Component`) and inject it. For now, this keeps the change localized and highlights the dependency.

**KISS:** It avoids modifying the `PriceFetcherConfig` or test setups that rely on the single-argument constructor.

### Why Math.max(4, this.fetchers.size())?

**Throughput:** To achieve true parallelism, the thread pool size should roughly match the number of concurrent tasks (IO-bound). If we have 10 fetchers but only 2 threads, we still bottleneck. This simple heuristic ensures we have enough capacity for our mock setup.

### Why did we remove the try-catch block from the loop?

**Refactoring:** The error handling logic (try-catch around `fetchPrice`) has been moved into `ManualConcurrentPriceEngine`. This cleans up the Service, allowing it to assume that `fetchPrices` returns a "clean" list of successful ticks. This adheres to the Single Responsibility Principle.

## Potential Reviewer/Interviewer Questions

### Q: By creating new ManualConcurrentPriceEngine inside the Service, haven't you tightly coupled them?

**A:** Yes, strict dependency injection is violated here. This is a deliberate "manual" step. To fix this, we would extract an interface (e.g., `PriceExecutionEngine`) and inject it via the constructor. This would allow us to swap in a `ReactivePriceEngine` or `AsyncPriceEngine` later for testing or production.

### Q: Does this implementation handle the "Thundering Herd" problem?

**A:** Not yet. If we receive 10,000 requests/second, we are calling `fetchPrices` 10,000 times. While `fetchPrices` uses a fixed thread pool, the queue for that pool could grow indefinitely, eventually causing `OutOfMemoryError`. We need strict bounds, timeouts, or backpressure (Reactive Streams) to handle high load safely.

### Q: How does this affect testability?

**A:** Because we instantiate the engine internally, we cannot easily mock it in `PriceServiceTest`. The tests now strictly run with the real `ManualConcurrentPriceEngine` (integration style). To restore unit test isolation, we must refactor to inject the engine.

