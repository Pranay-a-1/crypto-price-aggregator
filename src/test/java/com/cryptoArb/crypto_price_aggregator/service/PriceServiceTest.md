# PriceServiceTest Documentation

## Explain the Why

### Why checking `durationMs < (delayMs * 2)`?

With 3 fetchers taking 500ms each, **sequential execution takes 1500ms**. **Parallel execution takes ~500ms**. Setting the threshold at 1000ms (`delay * 2`) gives ample buffer for thread overhead while firmly rejecting sequential behavior.

### Why include `when(fetcher.getExchange()).thenReturn(exchange)` in the helper?

The service logs or uses the exchange name for error handling/results. Mocks return `null` by default for non-void methods, which might cause `NullPointerException` if the service tries to log `fetcher.getExchange()`.

### Why keep the old tests?

**Regression Testing:** We must ensure that adding parallelism doesn't break the core logic (aggregation, error handling, null checks).

---

## Potential Reviewer/Interviewer Questions

### Q: Are time-based tests reliable in CI/CD environments?

**A:** They can be flaky if the build agent is heavily loaded. However, checking for a 2x or 3x speedup difference is usually coarse enough to be stable. For strictly deterministic tests, we'd need to mock the `ExecutorService` or use a `VirtualTimeScheduler` (if using Reactive).

### Q: Why modify `PriceServiceImpl` directly instead of creating `ParallelPriceServiceImpl`?

**A:** This is a **Refactor/Evolution** step. If the requirement is "Make the application faster," we optimize the existing service. If we needed to support both modes simultaneously (e.g., via config), a **Strategy pattern** with two implementations would be better.

### Q: What if `Thread.sleep` throws `InterruptedException` in the mock?

**A:** The helper wraps it in a `RuntimeException`. In the actual service code, we expect `InterruptedException` to be handled gracefully (e.g., by logging and restoring the interrupt flag).

