package com.cryptoArb.core.internals;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



class SimpleJsonSerializerTest {

    private SimpleJsonSerializer serializer;
    private PriceTick testTick;

    @BeforeEach
    void setUp() {
        serializer = new SimpleJsonSerializer();

        testTick = new PriceTick(
                new CurrencyPair("BTC", "USD"),
                new Exchange("coinbase"),
                Instant.parse("2025-01-01T12:00:00Z"),
                new BigDecimal("50000"),
                new BigDecimal("50001")
        );
    }

    @Test
    @DisplayName("Should serialize a PriceTick object into a simple JSON string")
    void shouldSerializePriceTick() {
        // When: We call the serialize method (this line will also fail)
        String jsonOutput = serializer.serialize(testTick);

        // Then: Verify the JSON string contains the keys and correct values.
        // We check for 'contains' rather than an exact match, as the
        // order of fields from reflection isn't guaranteed.
        assertNotNull(jsonOutput);
        assertTrue(jsonOutput.startsWith("{") && jsonOutput.endsWith("}"), "JSON should start and end with braces");

        // Check for string/object values (which should be quoted)
        assertTrue(jsonOutput.contains("\"pair\":\"" + testTick.pair().toString() + "\""), "Missing or incorrect 'pair' field");
        assertTrue(jsonOutput.contains("\"exchange\":\"" + testTick.exchange().toString() + "\""), "Missing or incorrect 'exchange' field");
        assertTrue(jsonOutput.contains("\"timestamp\":\"" + testTick.timestamp().toString() + "\""), "Missing or incorrect 'timestamp' field");

        // Check for numeric values (which should not be quoted)
        assertTrue(jsonOutput.contains("\"bidPrice\":" + testTick.bidPrice().toString()), "Missing or incorrect 'bidPrice' field");
        assertTrue(jsonOutput.contains("\"askPrice\":" + testTick.askPrice().toString()), "Missing or incorrect 'askPrice' field");
    }
}