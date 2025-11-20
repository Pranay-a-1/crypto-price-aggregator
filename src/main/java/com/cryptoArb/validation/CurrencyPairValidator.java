package com.cryptoArb.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator logic for the @ValidCurrencyPair annotation.
 * Ensures string follows the pattern "XXX-YYY".
 */
public class CurrencyPairValidator implements ConstraintValidator<ValidCurrencyPair, String> {

    // Regex Explanation:
    // ^          : Start of string
    // [A-Z0-9]+  : One or more uppercase alphanumeric characters (BASE)
    // -          : Exact hyphen separator
    // [A-Z0-9]+  : One or more uppercase alphanumeric characters (QUOTE)
    // $          : End of string
    private static final Pattern PAIR_PATTERN = Pattern.compile("^[A-Z0-9]+-[A-Z0-9]+$");

    @Override
    public void initialize(ValidCurrencyPair constraintAnnotation) {
        // No initialization needed for this simple validator
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Standard Bean Validation practice: Allow nulls.
        // We rely on @NotNull or other annotations to check for presence.
        if (value == null) {
            return true;
        }

        return PAIR_PATTERN.matcher(value).matches();
    }
}