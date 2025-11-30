package com.cryptoArb.crypto_price_aggregator.exception;

/**
 * Custom exception for price fetching errors.
 * Following DRY principle: Single exception type for all fetch errors.
 * Extensible for future error codes or additional context.
 */
public class PriceFetchException extends Exception {

    public PriceFetchException(String message) {
        super(message);
    }

    public PriceFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
