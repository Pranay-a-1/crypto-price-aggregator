package com.cryptoArb.resilience;

import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.CoinbaseFetcher;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

/**
 * Integration test for Resilience4j Circuit Breaker.
 *
 * We configure the circuit breaker inline for this test to have
 * a small "sliding window" (5 calls) so we can trigger the open state quickly.
 *
 * Closed State (Normal): Requests go through.
 *
 * Open State (Failure): If the error rate exceeds a threshold (e.g., 50%), the circuit opens. Calls fail immediately (fast failure) without waiting for a timeout, preventing resource exhaustion.
 *
 * Half-Open State: After a wait duration, it allows a few requests to check if the external system has recovered.
 */
@SpringBootTest(properties = {
        "resilience4j.circuitbreaker.instances.coinbase.slidingWindowSize=5",
        "resilience4j.circuitbreaker.instances.coinbase.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.coinbase.waitDurationInOpenState=1000ms",
        "resilience4j.circuitbreaker.instances.coinbase.permittedNumberOfCallsInHalfOpenState=3"
})
class ResilienceTest {

    // We use  @MockitoSpyBean to wrap the real bean with a Mockito spy.
    // This allows us to force failures while keeping the AOP proxy chain intact.
    @MockitoSpyBean
    private CoinbaseFetcher coinbaseFetcher;

    @Test
    @DisplayName("Circuit Breaker should open after repeated failures")
    void shouldOpenCircuitBreakerOnFailures() throws Exception {
        // --- Given ---
        // We force the fetcher to throw an exception every time it's called
        doThrow(new PriceFetchException("Simulated Network Failure"))
                .when(coinbaseFetcher).fetchPrices();

        // --- When ---
        // We trigger enough failures to meet the threshold.
        // Window size = 5. Failure rate = 50%.
        // If we fail 5 times, we are at 100% failure rate -> Circuit should OPEN.
        for (int i = 0; i < 5; i++) {
            try {
                coinbaseFetcher.fetchPrices();
            } catch (Exception e) {
                // Ignore the expected exception for the setup phase
            }
        }

        // --- Then ---
        // The NEXT call should NOT execute the method logic.
        // Instead, the Circuit Breaker should intercept it and throw CallNotPermittedException.
        assertThrows(CallNotPermittedException.class, () -> {
            coinbaseFetcher.fetchPrices();
        }, "The circuit breaker should be OPEN and throw CallNotPermittedException");
    }
}


/**
 * Explain the Why
 * Why @SpyBean?
 *
 * CoinbaseFetcher is a Spring Bean. To apply the Circuit Breaker, Spring wraps it in an AOP proxy.
 *
 * If we used new CoinbaseFetcher(), we would bypass the proxy and the circuit breaker logic entirely.
 *
 * @MockitoSpyBean injects the existing proxy but allows us to override its behavior (force it to throw exceptions) for the test.
 *
 * Why define properties inline?
 *
 * Circuit breaker settings (like slidingWindowSize) are configuration. In a real app, the window might be 100 calls.
 *
 * For a unit/integration test, we don't want to wait for 100 calls. We override these settings to small numbers (5 calls) just for this test context to make it fast and deterministic.
 *
 * Why check for CallNotPermittedException?
 *
 * This is the specific signal from Resilience4j that "I stopped this request because the backend is down."
 *
 * If the test throws PriceFetchException, it means the circuit breaker is not working (the method was actually called).
 *
 * Potential Reviewer/Interviewer Questions
 * Why does this test fail right now?
 *
 * Answer: We haven't added the @CircuitBreaker(name = "coinbase") annotation to the CoinbaseFetcher class yet. Without that marker, Spring AOP doesn't know to apply the resilience logic, so the 6th call just goes through and throws the standard PriceFetchException.
 *
 * Why not just use try-catch logic inside the service?
 *
 * Answer: Hard-coded try-catch doesn't track state over time (rolling windows). It can catch one error, but it can't detect "the system is unstable" and stop sending traffic to let it recover. That state management is what a Circuit Breaker provides.
 *
 * What is the "Sliding Window"?
 *
 * Answer: It's the sample size used to calculate the failure rate. A COUNT_BASED window of 5 means "look at the last 5 calls." If 3 of them failed, the failure rate is 60%.
 */