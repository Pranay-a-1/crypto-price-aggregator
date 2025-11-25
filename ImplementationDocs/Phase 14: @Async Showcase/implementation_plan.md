# Phase 14: @Async Showcase Implementation Plan

## Goal

Complete the remaining Phase 14 tasks by implementing Spring's @Async functionality. This will demonstrate Spring's built-in concurrency abstractions by creating a `PriceReportService` that executes long-running tasks asynchronously on a custom thread pool.

## Background

Phase 14 aims to refactor from manual CompletableFuture-based concurrency to Spring's declarative @Async approach. The messaging refactor (producer/consumer with RabbitMQ) is already complete. The remaining work focuses on showcasing Spring @Async with custom thread pool configuration.

## User Review Required

> [!NOTE]
> This implementation follows TDD principles as specified in the user's custom rules. Each component will be created following the Red-Green-Refactor cycle, with comprehensive justification for design decisions.

## Proposed Changes

### Configuration Component

#### [NEW] [AsyncConfig.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/config/AsyncConfig.java)

**Purpose**: Configure Spring's async task execution with a custom thread pool.

**Implementation Details**:
- Annotate with `@Configuration` and `@EnableAsync(proxyTargetClass = true)`
- Create `ThreadPoolTaskExecutor` bean named `taskExecutor`
- Configure custom thread pool parameters:
  - Core pool size: 5 threads
  - Max pool size: 10 threads
  - Queue capacity: 100 tasks
  - Thread name prefix: "async-report-"
  - Rejection policy: CallerRunsPolicy (for graceful degradation)
- Include comprehensive logging in the bean creation

**Why This Approach?**:
- **Custom Thread Pool**: Separate thread pool for async tasks prevents interference with web request threads
- **Bounded Queue**: Prevents memory issues from unlimited queueing
- **Named Threads**: Makes debugging and profiling easier (can identify async threads in logs/dumps)
- **Rejection Policy**: CallerRunsPolicy provides backpressure - if all threads busy, caller executes task synchronously

---

### Service Component

#### [NEW] [ReportResult.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/domain_spring/ReportResult.java)

**Purpose**: Data transfer object for async report generation results.

**Implementation Details**:
- Java record with fields:
  - `String reportId`: Unique identifier for the report
  - `Instant generatedAt`: Timestamp of generation
  - `String reportType`: Type of report (e.g., "PRICE_SUMMARY")
  - `Long totalTicks`: Number of price ticks processed
  - `BigDecimal avgPrice`: Average price calculated
  - `String status`: Status (e.g., "COMPLETED", "FAILED")

**Why a Record?**:
- Immutability ensures thread-safety
- Compact syntax for DTOs
- Built-in equals/hashCode/toString for testing

#### [NEW] [PriceReportService.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/service/PriceReportService.java)

**Purpose**: Showcase Spring @Async functionality with a long-running report generation task.

**Implementation Details**:
- `@Service` annotated class
- Inject `PriceTickRepository` to query data
- Method: `CompletableFuture<ReportResult> generateReport(CurrencyPair pair)`
  - Annotate with `@Async("taskExecutor")` to use custom executor
  - Log thread name to prove execution on custom thread pool
  - Simulate long-running work with `Thread.sleep(3000)` 
  - Query database for price ticks for the given pair
  - Calculate statistics (count, average price)
  - Return `CompletableFuture.completedFuture(result)`
  
**Why CompletableFuture Return Type?**:
- Allows caller to chain/compose additional async operations
- Provides access to result in tests
- Best practice for @Async methods (alternative: void for fire-and-forget)

**Why Thread.sleep()?**:
- Simulates CPU/IO-intensive work (report generation, PDF creation, etc.)
- Makes async behavior observable in tests
- Demonstrates thread pool behavior under load

---

### Testing Component

#### [MODIFY] [RabbitMqMessagingIntegrationTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/integration/RabbitMqMessagingIntegrationTest.java)

**Modifications**: Add new test method for @Async functionality.

**New Test**: `testAsyncReportGeneration()`
- **Given**: Seed database with sample PriceTick data for a currency pair
- **When**: Call `priceReportService.generateReport(pair)` and immediately check return
- **Then (Immediate)**: Assert that method returns a CompletableFuture (not blocked)
- **Then (Async)**: Use Awaitility to wait for CompletableFuture completion (max 10 seconds)
- **Then (Result)**: Assert that `ReportResult` contains correct statistics
- **Then (Thread Verification)**: Verify log output shows execution on "async-report-" thread

**Why This Test?**:
- Proves method executes asynchronously (doesn't block caller)
- Validates custom thread pool configuration
- Demonstrates Awaitility for async testing
- Follows TDD principles from user's rules

## Verification Plan

### Automated Tests

**Test Command**:
```bash
cd /home/pranay/anotherDrive/javaCodes/crypto-price-aggregator
./mvnw test -Dtest=RabbitMqMessagingIntegrationTest#testAsyncReportGeneration
```

**Expected Outcomes**:
1. Test passes without timeout
2. Log output shows: `Thread name: async-report-X` (where X is thread number)
3. CompletableFuture completes with valid ReportResult
4. No blocking on caller thread

**Full Test Suite**:
```bash
./mvnw test
```

**Expected**: All existing tests pass + new async test passes

### Manual Verification

**Step 1**: Check thread pool creation in application logs
```bash
./mvnw spring-boot:run
# Look for: "Created ThreadPoolTaskExecutor with core=5, max=10"
```

**Step 2**: Verify async execution in logs
- Trigger report generation (via REST endpoint if we add one, or through existing flow)
- Check logs for thread names starting with "async-report-"
- Confirm execution happens on separate threads from "http-nio-" request threads

### Code Quality Verification

**Lint Check**:
```bash
./mvnw compile
```
**Expected**: No compilation errors or warnings

## Trade-offs and Alternatives

### Thread Pool Size Choice
**Decision**: Core=5, Max=10
**Rationale**: 
- Small enough for local dev/testing
- Large enough to handle moderate concurrent async tasks
- Can be externalized to application.properties in future
**Alternative**: Could use `@Value` to make configurable, but keeping simple for Phase 14 scope

### @Async Return Type
**Decision**: `CompletableFuture<ReportResult>`
**Rationale**:
- Caller can access result and compose operations
- Testable (can await completion)
**Alternative**: `void` for fire-and-forget, but less useful for demonstration/testing

### Simulation vs Real Work
**Decision**: Use Thread.sleep() to simulate long-running task
**Rationale**:
- Makes async behavior observable
- No external dependencies needed
- Clear demonstration of non-blocking execution
**Alternative**: Could generate actual PDF/Excel report, but adds complexity without educational value for Phase 14

## SOLID Principles Applied

- **Single Responsibility**: Each class has one clear purpose (config, service, DTO)
- **Open/Closed**: Can extend AsyncConfig to add more executors without modifying existing code
- **Dependency Inversion**: Service depends on repository interface, not concrete implementation
- **Interface Segregation**: Simple, focused interfaces for report generation

## Complexity Analysis

- **Time Complexity**: O(n) where n = number of PriceTicks queried from database
- **Space Complexity**: O(n) for query results, O(1) for final ReportResult
- **Thread Safety**: ReportResult is immutable; PriceReportService is stateless (thread-safe)
