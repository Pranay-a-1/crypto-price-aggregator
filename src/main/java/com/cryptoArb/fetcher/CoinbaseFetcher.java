package com.cryptoArb.fetcher;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CoinbaseFetcher implements PriceFetcher {

    // Logger to log messages for debugging and monitoring
    private static final Logger log = LoggerFactory.getLogger(CoinbaseFetcher.class);

    // 2. Apply Annotation
    // "coinbase" matches the config name in our test: resilience4j.circuitbreaker.instances.coinbase
    @Override
    @CircuitBreaker(name = "coinbase" , fallbackMethod = "fetchPricesFallback")
    public List<PriceTick> fetchPrices() throws PriceFetchException {
        // Minimal implementation
        // System.out.println("Fetching from Coinbase..."); // Replaced with logger in real app
        log.info("Fetching from Coinbase...");

        // Simulate network I/O delay as per Phase 6
        try {
            TimeUnit.MILLISECONDS.sleep(50); // Simulate 50ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceFetchException("Coinbase fetch interrupted", e);
        }

        return Collections.emptyList();
    }


    /**
     * 2. Fallback Method
     * Must have the SAME return type as the original method.
     * Must accept the Exception that caused the failure as the last argument.
     */
    public List<PriceTick> fetchPricesFallback(Exception ex) {
        // 3. Distinguish between "Circuit Open" and "Actual Failure"
        if (ex instanceof CallNotPermittedException) {
            // This happens when the circuit is OPEN (Fast Fail)
            log.warn("Coinbase circuit is OPEN. Skipping fetch request.");
        } else {
            // This happens when the call was attempted but failed (e.g., Timeout, 500 Error)
            log.error("Failed to fetch from Coinbase: {}. returning empty list.", ex.getMessage());
        }

        // Graceful degradation: Return empty list so the engine continues
        // so that it can continue to fetch prices from other exchanges
        return Collections.emptyList();
    }
}