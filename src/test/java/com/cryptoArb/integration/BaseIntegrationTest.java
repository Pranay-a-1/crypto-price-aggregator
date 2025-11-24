package com.cryptoArb.integration;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = CryptoPriceAggregatorApplication.class)
@Testcontainers
public abstract class BaseIntegrationTest {

    static {
        System.out.println("DOCKER_API_VERSION: " + System.getenv("DOCKER_API_VERSION"));
    }

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);
}
