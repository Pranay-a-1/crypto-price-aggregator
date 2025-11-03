package com.cryptoArb.exception;

/**
 * Unchecked exception for invalid configuration or inputs,
 * like an unknown currency pair or exchange ID.
 */
public class InvalidPairException extends RuntimeException {
    public InvalidPairException(String message) {
        super(message);
    }
}