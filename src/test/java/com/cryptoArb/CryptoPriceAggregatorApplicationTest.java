package com.cryptoArb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verify application context can load (no web server started).
 */
@SpringBootTest(classes = CryptoPriceAggregatorApplication.class)
class CryptoPriceAggregatorApplicationTest {

    @Test
    void contextLoads() {
        // empty — test passes if no exception is thrown during context startup
    }

}