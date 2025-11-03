package com.cryptoArb.exception;

// This is a checked exception, as planned, for forcing callers
// to handle potential fetching errors.
public class PriceFetchException extends Exception {
    public PriceFetchException(String message) {
        super(message);
    }

    public PriceFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
