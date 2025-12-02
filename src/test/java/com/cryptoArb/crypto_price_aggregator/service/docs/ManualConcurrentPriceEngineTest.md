# ManualConcurrentPriceEngine Test Explanation

## Explain the Why

### Why test with Thread.sleep?

This is the only reliable way in a unit test to distinguish between sequential execution (sum of delays) and parallel execution (max of delays) without inspecting thread names or using complex latching.

**Trade-off:** It makes the test slightly slower (500ms), but it provides a deterministic assertion of the NFR (Non-Functional Requirement) for concurrency.

### Why a separate ManualConcurrentPriceEngine class?

- **SRP (Single Responsibility Principle):** PriceService handles business logic (aggregation rules). The Engine handles the mechanics of threading (ExecutorService, Future, try-catch blocks).
- **Testability:** We can test threading logic in isolation from aggregation logic.
- **Evolution:** Later, we can swap this "Manual Engine" for a "Reactive Engine" or "CompletableFuture Engine" without touching the Service.

### Why pass List<PriceFetcher> to the method?

**Statelessness:** The engine doesn't need to "own" the fetchers. It just executes a task for whatever fetchers are provided. This makes the engine a pure worker component.

## Potential Reviewer/Interviewer Questions

### Q: Why use System.nanoTime() instead of System.currentTimeMillis()?

**A:** nanoTime() is monotonic and designed for measuring elapsed time, whereas currentTimeMillis() depends on the wall clock and can jump (e.g., NTP updates), making it unreliable for precise duration measurements.

### Q: You are creating new ManualConcurrentPriceEngine(4) in the test. Why not @Autowired?

**A:** For unit tests, I prefer manual instantiation to isolate the "Unit" under test. It avoids the overhead of the Spring Context and gives me explicit control over the constructor arguments (like thread pool size) for the specific test scenario.

### Q: What happens if fetchPrice hangs indefinitely?

**A:** In this manual implementation using ExecutorService, if we don't set a timeout on future.get(), the main thread could block forever. In the implementation phase (Green), I will need to ensure we handle timeouts or allow the invokeAll to manage them. The @Timeout on the test serves as a safety net for the test suite itself.
