package com.cryptoArb.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception handling logic for the API.
 *<p>
 * This class uses @RestControllerAdvice to act as an Aspect that intercepts
 * exceptions thrown by ANY controller in the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles PriceNotFoundException (404 Not Found).
     *
     * @param ex      The exception that was thrown.
     * @param request The HTTP request (used to get the path).
     * @return A standardized JSON error response.
     */
    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePriceNotFound(
            PriceNotFoundException ex, HttpServletRequest request) {

        // 1. Create the error body
        // We use LinkedHashMap to preserve field order for readability
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        // 2. Return the ResponseEntity with the correct status code
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // In a future Refactor step, we would add a generic Exception.class handler here
    // to catch unexpected 500 errors.
}