# Phase 7 Implementation Plan

## 1. Dependencies and Configuration
- [x] Add `resilience4j-spring-boot3` and `spring-boot-starter-aop` to `pom.xml`.
- [x] Configure Resilience4j in `application.properties`:
    -   Circuit Breaker: Sliding window size 10, failure rate 50%, wait duration 5s.
    -   Retry: Max attempts 3, exponential backoff (500ms * 2).
- [x] Add Chaos configuration properties (`chaos.mode.enabled`, `chaos.latency.min`, `chaos.failure.rate`) to `application.properties`.

## 2. Manual Resilient Fetcher (Educational)
- [x] Create `ManualResilientBinanceFetcher`.
- [x] Implement `injectChaos()` method using `java.util.Random` and `Thread.sleep` based on config properties.
- [x] Implement a `while` loop with `try-catch` to manually retry failed requests.
- [x] Implement manual exponential backoff using `Thread.sleep`.
- [x] Log attempts and failures explicitly to show the "noise" of manual handling.

## 3. Professional Fetchers (Resilience4j)
- [x] Update `BinanceFetcher`, `CoinbaseFetcher`, and `KrakenFetcher`.
- [x] Inject `chaos.mode.enabled` and other chaos properties.
- [x] Add `injectChaos()` check at the start of `fetchPrice`.
- [x] Annotate `fetchPrice` with `@CircuitBreaker(name = "...")` and `@Retry(name = "...")`.
- [x] Ensure `PriceFetchException` is thrown to trigger the circuit breaker.

## 4. Testing
- [x] Create `ResilienceIntegrationTest`.
    -   Use `@TestPropertySource` to enable chaos and force 100% failure rate for deterministic testing.
    -   Call fetchers repeatedly in a loop.
    -   Verify `CallNotPermittedException` is thrown, indicating the Circuit Breaker has opened.
- [x] Create `ManualResilientBinanceFetcherTest`.
    -   Use `ReflectionTestUtils` to disable chaos for unit testing correctness.
    -   Verify basic fetch and event publishing works.

## 5. Documentation
- [x] Create `walkthrough.md` explaining the "Manual vs Professional" approach.
- [x] Create `phase_7_summary.md` summarizing the changes.
