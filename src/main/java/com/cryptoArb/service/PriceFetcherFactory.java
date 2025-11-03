package com.cryptoArb.service;

import com.cryptoArb.exception.InvalidPairException;
import com.cryptoArb.fetcher.BinanceFetcher;
import com.cryptoArb.fetcher.CoinbaseFetcher;
import com.cryptoArb.fetcher.PriceFetcher;

// This is our "Factory"
public class PriceFetcherFactory {

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
            throw new InvalidPairException("Exchange ID cannot be null");
        }

        // Use a modern Java switch expression for better readability
        return switch (exchangeId.toLowerCase()) {
            case "coinbase" -> new CoinbaseFetcher();
            case "binance" -> new BinanceFetcher();
            // The default case now throws our custom exception
            default -> throw new InvalidPairException("No fetcher available for exchange: " + exchangeId);
        };


    }
}