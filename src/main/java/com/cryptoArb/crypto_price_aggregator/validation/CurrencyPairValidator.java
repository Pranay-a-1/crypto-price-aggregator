package com.cryptoArb.crypto_price_aggregator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CurrencyPairValidator implements ConstraintValidator<ValidCurrencyPair, String> {

    // Regex: 3-4 uppercase letters, forward slash, 3-4 uppercase letters
    // Example: BTC/USD, USDT/EUR
    private static final String CURRENCY_PAIR_PATTERN = "^[A-Z]{3,4}/[A-Z]{3,4}$";
    private static final Pattern PATTERN = Pattern.compile(CURRENCY_PAIR_PATTERN);

    @Override
    public void initialize(ValidCurrencyPair constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return PATTERN.matcher(value).matches();
    }
}
