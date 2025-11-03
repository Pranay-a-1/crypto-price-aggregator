package com.cryptoArb.service;

import com.cryptoArb.exception.InvalidPairException;
import com.cryptoArb.fetcher.BinanceFetcher;
import com.cryptoArb.fetcher.CoinbaseFetcher;
import com.cryptoArb.fetcher.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This is our "Factory"
public class PriceFetcherFactory {

    // 2. Set up the static logger instance
    private static final Logger log = LoggerFactory.getLogger(PriceFetcherFactory.class);

    public PriceFetcher createFetcher(String exchangeId) {

        /* Old implementation (kept for reference) - inefficient:
           - Hardcoded mapping of exchange IDs to concrete fetcher classes.
           - Requires modifying this factory to add new exchanges (violates open/closed principle).
           Prefer a registration-based approach or dependency injection to avoid editing code for each new exchange.
        */
        /*
        if ("coinbase".equalsIgnoreCase(exchangeId)) {
            return new CoinbaseFetcher();
        } else if ("binance".equalsIgnoreCase(exchangeId)) {
            return new BinanceFetcher();
        }

         return null; // Our current test doesn't check for this case yet
        */

        if (exchangeId == null) {
            log.warn("createFetcher called with null exchangeId"); // Log a warning
            throw new InvalidPairException("Exchange ID cannot be null");
        }

        log.info("Creating fetcher for exchange: {}", exchangeId); // Log an info message

        // Use a modern Java switch expression for better readability
        return switch (exchangeId.toLowerCase()) {
            case "coinbase" -> new CoinbaseFetcher();
            case "binance" -> new BinanceFetcher();
            // The default case now throws our custom exception

            default -> {
                log.error("No fetcher implementation found for: {}", exchangeId); // Log an error
                throw new InvalidPairException("No fetcher available for exchange: " + exchangeId);
            }
        };


    }
}