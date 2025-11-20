package com.cryptoArb.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeHealthIndicatorTest {

    @Test
    void healthShouldBeUp() {
        ExchangeHealthIndicator indicator = new ExchangeHealthIndicator();
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("service", "Exchange Connectivity");
    }
}