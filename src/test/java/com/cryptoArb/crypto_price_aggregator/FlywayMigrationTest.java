package com.cryptoArb.crypto_price_aggregator;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Flyway database migrations.
 * 
 * Following TDD: Test that migrations execute successfully and create expected
 * schema.
 * Extends BaseIntegrationTest to use Testcontainers with PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    void shouldHaveFlywayConfigured() {
        // Verify Flyway bean is available
        assertNotNull(flyway, "Flyway should be auto-configured");
    }

    @Test
    void shouldRunMigrationsSuccessfully() {
        // Given - Flyway should have run migrations automatically on startup
        assertNotNull(flyway);

        // When - Get migration info
        var info = flyway.info();
        var migrations = info.all();

        // Then - Verify migrations executed
        assertTrue(migrations.length >= 2, "Should have at least 2 migrations (V1 and V2)");

        // Verify V1 migration
        var v1 = info.getInfoResult().migrations.stream()
                .filter(m -> m.version.equals("1"))
                .findFirst();
        assertTrue(v1.isPresent(), "V1 migration should exist");
        assertEquals("initial schema", v1.get().description);

        // Verify V2 migration
        var v2 = info.getInfoResult().migrations.stream()
                .filter(m -> m.version.equals("2"))
                .findFirst();
        assertTrue(v2.isPresent(), "V2 migration should exist");
        assertEquals("create arbitrage opportunities", v2.get().description);
    }

    @Test
    void shouldHaveCorrectMigrationState() {
        // Given
        assertNotNull(flyway);

        // When
        var info = flyway.info();

        // Then - All pending migrations should be applied
        assertEquals(0, info.pending().length, "There should be no pending migrations");

        // All migrations should be successful
        assertTrue(info.applied().length >= 2, "At least 2 migrations should be applied");
    }

    @Test
    void shouldValidateMigrationsWithoutErrors() {
        // Given
        assertNotNull(flyway);

        // When & Then - Validate should not throw exception
        assertDoesNotThrow(() -> flyway.validate(),
                "Migration validation should pass without errors");
    }
}
