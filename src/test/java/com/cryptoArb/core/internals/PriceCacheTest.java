package com.cryptoArb.core.internals;

import com.cryptoArb.domain_records.ConsolidatedPrice;
import com.cryptoArb.domain_records.CurrencyPair;
import com.cryptoArb.domain_records.Exchange;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCacheTest {

    private PriceCache priceCache;
    private ConsolidatedPrice testPrice;

    @BeforeEach
    void setUp() {
        priceCache = new PriceCache();

        // Create a dummy price object to store as the value
        // ConsolidatedPrice is a domain object that represents a price which is a bid and ask price from different exchanges
        testPrice = new ConsolidatedPrice(
                new CurrencyPair("BTC", "USD"),
                Instant.now(),
                new BigDecimal("50000"),
                new Exchange("test-bid"),
                new BigDecimal("50001"),
                new Exchange("test-ask")
        );
    }

    @Test
    @DisplayName("Should auto-remove entry when key is no longer strongly referenced")
    void shouldGarbageCollectEntryWhenKeyIsNulled() {
        // 1. Create a strong reference to the key
        CurrencyPair key = new CurrencyPair("BTC", "USD");

        // 2. Put it in the cache
        priceCache.put(key, testPrice);

        // 3. Verify it's there
        assertEquals(1, priceCache.size(), "Cache should have 1 entry");

        // 4. Remove the *only* strong reference to the key
        key = null;

        // 5. Hint to the JVM to run garbage collection
        // This is just a hint, not a guarantee.
        System.gc();

        // 6. Use Awaitility to wait until the GC runs and clears the WeakHashMap
        Awaitility.await()
                .atMost(Duration.ofSeconds(10)) // Wait a max of 10s
                .pollInterval(Duration.ofMillis(100)) // Check every 100ms
                .until(() -> priceCache.size() == 0); // The success condition

        // If Awaitility doesn't time out, this assertion will pass.
        assertEquals(0, priceCache.size(), "Cache should be empty after GC");
    }
}