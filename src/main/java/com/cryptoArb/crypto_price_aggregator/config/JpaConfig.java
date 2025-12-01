package com.cryptoArb.crypto_price_aggregator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration for the application.
 * 
 * Configures:
 * - JPA Repository scanning
 * - Transaction management
 * 
 * Following Spring best practices:
 * - Explicit configuration for clarity
 * - Enables declarative transaction management with @Transactional
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.cryptoArb.crypto_price_aggregator.repository")
@EnableTransactionManagement
public class JpaConfig {
    // No additional beans needed - Spring Boot auto-configuration handles
    // EntityManagerFactory, DataSource, etc.
}
