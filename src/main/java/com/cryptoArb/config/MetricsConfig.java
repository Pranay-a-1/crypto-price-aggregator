package com.cryptoArb.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Micrometer Metrics.
 *
 * This class enables the AOP-based @Timed annotation support.
 */
@Configuration
public class MetricsConfig {

    /**
     * Registers the TimedAspect bean.
     *
     * DECISION: Explicitly define TimedAspect.
     * WHY: Spring Boot does not auto-configure this bean to avoid imposing
     * AOP overhead on applications that don't use it.
     *
     * How it works:
     * 1. It creates an AOP proxy around beans with @Timed methods.
     * 2. It intercepts calls, measuring execution time.
     * 3. It records the stats (count, max, total time) to the MeterRegistry.
     *
     * @param registry The auto-configured MeterRegistry (e.g., Prometheus)
     * @return The aspect that handles the @Timed logic.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}