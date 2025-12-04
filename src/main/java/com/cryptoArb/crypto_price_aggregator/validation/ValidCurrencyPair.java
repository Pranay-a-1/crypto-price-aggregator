package com.cryptoArb.crypto_price_aggregator.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = CurrencyPairValidator.class)
@Documented
public @interface ValidCurrencyPair {

    String message() default "Invalid currency pair format. Expected format: BASE/QUOTE (e.g., BTC/USD)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
