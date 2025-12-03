package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "chaos.mode.enabled=true",
        "resilience4j.circuitbreaker.configs.default.slidingWindowSize=5",
        "resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=3",
        "resilience4j.circuitbreaker.configs.default.failureRateThreshold=50"
})
class ResilienceIntegrationTest {

    @Autowired
    private BinanceFetcher binanceFetcher;

    @Autowired
    private CoinbaseFetcher coinbaseFetcher;

    @Autowired
    private KrakenFetcher krakenFetcher;

    @Test
    void binanceFetcher_CircuitBreakerOpens_AfterRepeatedFailures() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        verifyCircuitBreakerOpens(binanceFetcher, pair);
    }

    @Test
    void coinbaseFetcher_CircuitBreakerOpens_AfterRepeatedFailures() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        verifyCircuitBreakerOpens(coinbaseFetcher, pair);
    }

    @Test
    void krakenFetcher_CircuitBreakerOpens_AfterRepeatedFailures() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        verifyCircuitBreakerOpens(krakenFetcher, pair);
    }

    private void verifyCircuitBreakerOpens(PriceFetcher fetcher, CurrencyPair pair) {
        // Attempt calls until the circuit opens
        boolean circuitOpened = false;
        // We try more than the sliding window size
        for (int i = 0; i < 10; i++) {
            try {
                fetcher.fetchPrice(pair);
            } catch (Exception e) {
                if (e instanceof CallNotPermittedException) {
                    circuitOpened = true;
                    break;
                }
                // Otherwise it's the expected PriceFetchException from Chaos
            }
        }
        assertTrue(circuitOpened, "Circuit breaker should have opened after repeated failures");
    }
}
