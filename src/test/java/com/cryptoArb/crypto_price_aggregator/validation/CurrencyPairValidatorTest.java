package com.cryptoArb.crypto_price_aggregator.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CurrencyPairValidatorTest {

    private CurrencyPairValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new CurrencyPairValidator();
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPairIsValid() {
        assertTrue(validator.isValid("BTC/USD", context));
        assertTrue(validator.isValid("ETH/EUR", context));
        assertTrue(validator.isValid("LTC/BTC", context));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenFormatIsInvalid() {
        assertFalse(validator.isValid("BTC-USD", context));
        assertFalse(validator.isValid("BTCUSD", context));
        assertFalse(validator.isValid("BTC/USD/EUR", context));
        assertFalse(validator.isValid("", context));
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenCurrencyIsNotSupported() {
        // Assuming we only support standard crypto/fiat for now, or just checking
        // format + 3-4 chars
        // For this phase, let's enforce 3-4 uppercase letters
        assertFalse(validator.isValid("btc/usd", context)); // Lowercase
        assertFalse(validator.isValid("BITCOIN/USD", context)); // Too long
        assertFalse(validator.isValid("B/U", context)); // Too short
        assertFalse(validator.isValid("123/456", context)); // Numbers
    }
}
