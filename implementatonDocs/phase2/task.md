# Phase 2: Manual Concurrency for Multi-Exchange Fetching - Task Breakdown

## Cycle 1: VolatileFlagStop (Thread-Safe Shutdown Signal)

- **RED**: Write `VolatileFlagStopTest` - test multiple threads reading stop flag
- **GREEN**: Implement `VolatileFlagStop` with volatile boolean
- **REFACTOR**: Add logging for educational debugging
- ✓ Verify all tests pass

## Cycle 2: MyBlockingQueue (Thread-Safe Bounded Buffer)

- **RED**: Write `MyBlockingQueueTest` - test concurrent put/take operations
- **GREEN**: Implement `MyBlockingQueue` with synchronized methods and wait/notify
- **REFACTOR**: Extract capacity logic, add timeout handling
- ✓ Verify all tests pass

## Cycle 3: Multiple Mock Exchanges

- **RED**: Update `PriceServiceImplTest` - test with 4 mock exchanges (expect failure)
- **GREEN**: Create 4 `MockPriceFetcher` instances with different exchange names
- **REFACTOR**: Use factory pattern for fetcher creation
- ✓ Verify tests pass

## Cycle 4: ManualConcurrentPriceEngine

- **RED**: Write `ManualConcurrentPriceEngineTest` - test parallel fetching
- **GREEN**: Implement `ManualConcurrentPriceEngine` with `ExecutorService`
- **REFACTOR**: Extract thread pool configuration
- ✓ Verify tests pass

## Cycle 5: Integration with PriceService

- **RED**: Update service tests to expect faster parallel execution
- **GREEN**: Integrate engine into `PriceServiceImpl`
- **REFACTOR**: Add error handling for failed tasks
- ✓ Verify all tests pass

## Cycle 6: Benchmark Comparison

- **RED**: Write `BenchmarkRunnerTest` - verify performance comparison
- **GREEN**: Create `BenchmarkRunner` comparing sequential vs. parallel
- **REFACTOR**: Add statistical analysis (mean, stddev)
- ✓ Verify performance improvement

## Verification Checklist

- [ ] All unit tests pass
- [ ] Integration tests pass
- [ ] Benchmark shows ~2x speedup
- [ ] No thread leaks (verify with JConsole/VisualVM)
- [ ] Logs show concurrent thread execution
