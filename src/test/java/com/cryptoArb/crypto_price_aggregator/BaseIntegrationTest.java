package com.cryptoArb.crypto_price_aggregator;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers with PostgreSQL.
 * <p>
 * This class provides:
 * - A shared PostgreSQL container for all integration tests
 * - Container reuse across test classes for performance
 * - Dynamic property configuration for Spring datasource
 * <p>
 * All integration tests should extend this class to ensure they run
 * against a real PostgreSQL instance via Testcontainers.
 * <p>
 * Following TDD: This enables GREEN phase by providing real PostgreSQL
 * for our integration tests without manual database setup.
 * <p>
 * * Supports two modes:
 * 1. Testcontainers (Default): Uses ephemeral PostgreSQL container.
 * 2. External DB (Profile "docker-env"): Uses external DB (e.g., from docker-compose).
 *    Set active profile to "docker-env" or "test-docker" to bypass Testcontainers.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final boolean USE_TESTCONTAINERS;

    static {
        // Check if we should skip testcontainers (e.g. running inside docker)
        String useExternalDb = System.getProperty("use.external.db");
        USE_TESTCONTAINERS = !Boolean.parseBoolean(useExternalDb);
    }

    static PostgreSQLContainer<?> postgres;

    static {
        if (USE_TESTCONTAINERS) {
            postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("cryptodb_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);
            postgres.start();
        }
    }

    /**
     * Configures Spring Boot properties dynamically from the Testcontainer.
     * This allows tests to connect to the ephemeral PostgreSQL instance.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (USE_TESTCONTAINERS) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        } else {
            // Configuration for external DB (docker-compose)
            // Assumes running inside docker network where 'db' resolves
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://db:5432/cryptodb");
            registry.add("spring.datasource.username", () -> "cryptouser");
            registry.add("spring.datasource.password", () -> "cryptopass");
        }

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Disable Flyway in tests - we use Hibernate's create-drop for test isolation
        registry.add("spring.flyway.enabled", () -> "false");

        // Disable RabbitMQ for integration tests if not needed
        registry.add("spring.rabbitmq.host", () -> "rabbitmq"); // Default to rabbitmq service name
    }

}
