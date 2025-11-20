package com.cryptoArb.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Handles validation errors for @Validated controllers (AOP based).
     * Thrown when @ValidCurrencyPair fails on a @PathVariable.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        // 1. Extract the messages from the violations
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));

        // 2. Create the standard error body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", message);
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic validation errors (Spring 6.1+ native support).
     * Kept for future-proofing if we move validation logic.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        // 1. Extract the user-friendly message from the validation result
        // REFACTOR: Use getParameterValidationResults() instead of deprecated getAllValidationResults()
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // 2. Create the standard error body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", message);
        body.put("path", request.getRequestURI());

        // 3. Return 400 Bad Request
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}