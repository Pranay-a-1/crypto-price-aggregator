package com.cryptoArb.exception;

/**
 * Exception thrown when price data cannot be found for a requested pair.
 *<p>
 * Design Decision: Extends RuntimeException (Unchecked)
 * Why? In modern Spring applications, business logic errors are typically
 * unchecked. This avoids cluttering service signatures with 'throws'
 * and allows the @ControllerAdvice to handle them centrally.
 */
public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(String message) {
        super(message);
    }
}