# Phase 7: Resilience with Artificial Failures and Circuit Breakers

## Goal
Inject failures and latency to demonstrate the need for resilience, then implement robust error handling using Resilience4j (Circuit Breaker and Retry patterns).

## Changes

### 1. Manual Chaos & Resilience (Evolutionary Step)
*   **`ManualResilientBinanceFetcher`**:
    *   Created a manual implementation of a fetcher that simulates "chaos" (random failures and latency).
    *   Implemented a manual retry loop with exponential backoff (`while` loop + `Thread.sleep`) to demonstrate the "pain" of writing resilience logic from scratch.
    *   Configurable chaos parameters via `application.properties` (`chaos.latency.min`, `chaos.failure.rate`).

### 2. Professional Resilience with Resilience4j
*   **Dependencies**: Added `resilience4j-spring-boot3` and `spring-boot-starter-aop` to `pom.xml`.
*   **Annotation-based Resilience**:
    *   Updated `BinanceFetcher`, `CoinbaseFetcher`, and `KrakenFetcher` to use `@Retry` and `@CircuitBreaker` annotations.
    *   Refactored fetchers to support a "Chaos Mode" (toggled via `chaos.mode.enabled`) which injects artificial latency (2000ms+) and failures (50% probability) to verify resilience mechanisms.
*   **Configuration**:
    *   Configured Circuit Breaker settings in `application.properties` (sliding window size: 10, failure rate threshold: 50%).
    *   Configured Retry settings (max attempts: 3, exponential backoff).

### 3. Testing
*   **`ResilienceIntegrationTest`**:
    *   Created an integration test that enables chaos mode and verifies that the Circuit Breaker correctly opens (`CallNotPermittedException`) after repeated failures.
    *   Configured test-specific properties to force failures for deterministic testing.
*   **`ManualResilientBinanceFetcherTest`**:
    *   Unit tests to verify the manual fetcher's behavior (though difficult to test randomness deterministically).

## Verification
*   **Chaos Injection**: Verified that fetchers respect the configured latency and failure rates when chaos mode is enabled.
*   **Circuit Breaking**: Verified that repeated failures trigger the circuit breaker, stopping further calls to the failing "exchange".

## Why This Matters
Real-world exchanges are unreliable. API calls fail, timeout, or return errors. Without resilience (retries, circuit breakers), a single failing dependency can cascade and crash the entire application or exhaust resources (threads). This phase moves us from "happy path" coding to production-grade robustness.
