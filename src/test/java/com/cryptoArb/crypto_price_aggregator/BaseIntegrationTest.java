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
 * 
 * This class provides:
 * - A shared PostgreSQL container for all integration tests
 * - Container reuse across test classes for performance
 * - Dynamic property configuration for Spring datasource
 * 
 * All integration tests should extend this class to ensure they run
 * against a real PostgreSQL instance via Testcontainers.
 * 
 * Following TDD: This enables GREEN phase by providing real PostgreSQL
 * for our integration tests without manual database setup.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    /**
     * Shared PostgreSQL container for all integration tests.
     * Uses postgres:15-alpine image for production parity.
     * Container is reusable across test classes to improve performance.
     * Note: Testcontainers manages the container lifecycle, no need to close
     * manually.
     */
    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("cryptodb_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Configures Spring Boot properties dynamically from the Testcontainer.
     * This allows tests to connect to the ephemeral PostgreSQL instance.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Disable RabbitMQ for integration tests if not needed
        registry.add("spring.rabbitmq.host", () -> "localhost");
    }
}
