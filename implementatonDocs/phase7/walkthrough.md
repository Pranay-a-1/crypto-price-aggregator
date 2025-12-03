# Phase 7 Walkthrough: Resilience with Artificial Failures and Circuit Breakers

## Prerequisite
Ensure Phase 6 is complete (RabbitMQ decoupling).

## Step 1: Dependencies
Add Resilience4j and AOP to `pom.xml`.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
```

## Step 2: The "Painful" Manual Implementation
We created `ManualResilientBinanceFetcher` to understand what we are automating.
*   **Chaos**: Added `injectChaos()` to sleep randomly and throw exceptions.
*   **Retry**: Wrote a `while` loop with `Thread.sleep` for backoff.
*   **Lesson**: Manual retry logic is verbose, error-prone (handling interrupts, exponential math), and pollutes business logic.

## Step 3: The "Professional" Implementation (Resilience4j)
We refactored `BinanceFetcher`, `CoinbaseFetcher`, and `KrakenFetcher`.
1.  **Annotations**: Added `@CircuitBreaker(name = "binance")` and `@Retry(name = "binance")` to `fetchPrice`.
2.  **Chaos Mode**: Implemented `chaos.mode.enabled` check. If true, the fetcher sleeps for `chaos.latency.min` (default 2000ms) + random jitter, and throws an exception with `chaos.failure.rate` (default 50%) probability.

## Step 4: Configuration
Added to `application.properties`:
*   **Circuit Breaker**: `slidingWindowSize=10`, `failureRateThreshold=50`.
*   **Retry**: `maxAttempts=3`, `waitDuration=500ms`.
*   **Chaos**: `chaos.mode.enabled=false`, `chaos.latency.min=2000`, `chaos.failure.rate=50`.

## Step 5: Integration Testing
Created `ResilienceIntegrationTest`.
*   Enables chaos mode.
*   Forces 100% failure rate (in test config).
*   Calls the fetcher in a loop.
*   Asserts that `CallNotPermittedException` is eventually thrown (Circuit Breaker open).

## How to Verify
Run the test:
```bash
./mvnw test -Dtest=ResilienceIntegrationTest
```

Expected Output:
*   Tests pass.
*   Logs show "Chaos Monkey: Simulating failure..."
*   Logs show circuit breaker state transitions (if enabled).

## Next Steps
In Phase 8, we will add observability (metrics, tracing) to see these circuit breaker events in real-time without grepping logs.
