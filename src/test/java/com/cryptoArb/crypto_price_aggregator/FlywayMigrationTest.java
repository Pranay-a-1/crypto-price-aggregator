package com.cryptoArb.crypto_price_aggregator;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Flyway database migrations.
 * 
 * Following TDD: Test that migrations execute successfully and create expected
 * schema.
 * 
 * Note: This test deliberately does NOT extend BaseIntegrationTest because
 * BaseIntegrationTest disables Flyway for all other integration tests.
 * We need Flyway enabled to test the migration process.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class FlywayMigrationTest {

    static final boolean USE_TESTCONTAINERS;

    static {
        // Check if we should skip testcontainers (e.g. running with external DB)
        String useExternalDb = System.getProperty("use.external.db");
        USE_TESTCONTAINERS = !Boolean.parseBoolean(useExternalDb);
    }

    static PostgreSQLContainer<?> postgres;

    static {
        if (USE_TESTCONTAINERS) {
            postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("cryptodb_test")
                    .withUsername("test")
                    .withPassword("test");
            postgres.start();
        }
    }

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private Flyway flyway;

    /**
     * Configure Spring Boot properties for this Flyway-specific test.
     * Unlike BaseIntegrationTest, we enable Flyway and set Hibernate to validate
     * mode.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (USE_TESTCONTAINERS) {
            // Configure PostgreSQL datasource from Testcontainer
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        } else {
            // Configuration for external DB (docker-compose)
            // Connects to Docker services exposed on localhost via port mapping
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/cryptodb");
            registry.add("spring.datasource.username", () -> "cryptouser");
            registry.add("spring.datasource.password", () -> "cryptopass");
        }

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        // CRITICAL: Enable Flyway and disable Hibernate DDL auto-generation
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        // Flyway configuration
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.baseline-version", () -> "0");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        // RabbitMQ configuration
        if (USE_TESTCONTAINERS) {
            registry.add("spring.rabbitmq.host", () -> "rabbitmq");
        } else {
            registry.add("spring.rabbitmq.host", () -> "localhost");
        }
    }

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
