# Phase 7: Resilience with Artificial Failures and Circuit Breakers

## Goal
Inject failures and latency into the system to demonstrate the fragility of distributed systems, then implement robust error handling using Resilience4j (Circuit Breaker and Retry patterns) to ensure the application remains stable under stress.

## Requirements

1.  **Dependencies**: Add `resilience4j-spring-boot3` and `spring-boot-starter-aop` to the project.
2.  **Manual "Pain" Implementation**:
    *   Create a `ManualResilientBinanceFetcher` that does *not* use Resilience4j.
    *   Implement manual retry logic using loops and `Thread.sleep` (demonstrating exponential backoff manually).
    *   This fetcher should be isolated (e.g., separate bean) or swappable for demonstration.
3.  **Chaos Injection**:
    *   All fetchers (Manual and Professional) must support a "Chaos Mode".
    *   **Latency**: When enabled, inject random sleep (e.g., min 2000ms + jitter).
    *   **Failures**: When enabled, throw random exceptions (e.g., 50% probability).
    *   This behavior should be configurable via `application.properties` (e.g., `chaos.mode.enabled`, `chaos.failure.rate`).
4.  **Professional Resilience**:
    *   Refactor `BinanceFetcher`, `CoinbaseFetcher`, and `KrakenFetcher`.
    *   Apply `@CircuitBreaker` to handle failures (open circuit after threshold).
    *   Apply `@Retry` to handle transient failures before giving up.
    *   Configure sliding windows and thresholds in `application.properties`.
5.  **Testing**:
    *   Create integration tests that enable Chaos Mode.
    *   Verify that the Circuit Breaker opens (`CallNotPermittedException`) after repeated failures.
    *   Ensure the application doesn't crash during these failures.
