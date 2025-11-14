package com.cryptoArb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is our first test in Phase 11.
 * PURPOSE: To verify that the Spring Boot application context can load.
 * ANNOTATION: @SpringBootTest is a powerful annotation. It tells Spring to
 * start up the *entire* application context, just like it would in production.
 */
@SpringBootTest
class CryptoPriceAggregatorApplicationTest {

    /**
     * TEST: contextLoads()
     * GIVEN: A project with Spring Boot dependencies
     * WHEN: The @SpringBootTest runner executes
     * THEN: The test should pass if the context loads, and fail if it doesn't.
     */
    @Test
    void contextLoads() {
        // This test is empty on purpose!
        // Its only job is to *not* throw an exception during startup.
        // If the context fails to load, this test will fail automatically.
    }

}