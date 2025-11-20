package com.cryptoArb.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for Currency Pair strings.
 * Expects format: "BASE-QUOTE" (e.g., "BTC-USD").
 */
@Documented
@Constraint(validatedBy = CurrencyPairValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD}) // Can be used on method params or fields
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrencyPair {

    // The default error message if validation fails
    String message() default "Invalid currency pair format (use BASE-QUOTE)";

    // Boilerplate for validation groups (required by Jakarta Validation)
    Class<?>[] groups() default {};

    // Boilerplate for metadata payloads (required by Jakarta Validation)
    Class<? extends Payload>[] payload() default {};
}