package com.cryptoArb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * DECISION: Add @SpringBootApplication
 * WHY: This annotation marks this class as the main entry point for the
 * Spring Boot application. It enables auto-configuration and component scanning,
 * which is what our failing test (@SpringBootTest) is searching for.
 */
@SpringBootApplication // 2. Add this annotation
@EntityScan("com.cryptoArb.domain_spring")
public class CryptoPriceAggregatorApplication {

    /**
     * DECISION: Use SpringApplication.run()
     * WHY: This static method is the standard way to launch the application.
     * It creates the application context, runs auto-configuration,
     * and starts any embedded servers (like Tomcat).
     */
    public static void main(String[] args) {

        SpringApplication.run(CryptoPriceAggregatorApplication.class, args); // 3. Add this line
    }

}
