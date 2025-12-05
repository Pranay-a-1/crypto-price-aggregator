package com.cryptoArb.crypto_price_aggregator.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics Configuration using Micrometer
 *
 * Phase 8 - Observability:
 * - Defines custom metrics for price fetching and aggregation
 * - Uses Micrometer MeterRegistry for registry abstraction
 * - Metrics can be exported to Prometheus, Graphite, etc.
 *
 * Why Micrometer: Industry standard for metrics in Spring Boot
 * Provides vendor-neutral abstraction over different monitoring systems
 *
 * Design Decisions:
 * - Counter metrics for tracking success/failure counts
 * - Tagged metrics (exchange name) for granular monitoring
 * - Beans for easy injection into services
 */
@Configuration
public class MetricsConfig {

    /**
     * Counter for successful price fetches per exchange
     * Usage: Inject and call counter.increment() on successful fetch
     */
    public Counter priceFetchSuccessCounter(MeterRegistry registry, String exchangeName) {
        return registry.counter("price.fetch.success", "exchange", exchangeName);
    }

    /**
     * Counter for failed price fetches per exchange
     * Usage: Inject and call counter.increment() on fetch failure
     */
    public Counter priceFetchFailureCounter(MeterRegistry registry, String exchangeName) {
        return registry.counter("price.fetch.failure", "exchange", exchangeName);
    }

    /**
     * Counter for total price aggregations
     * Usage: Inject and call counter.increment() when aggregating prices
     */
    public Counter priceAggregationCounter(MeterRegistry registry) {
        return registry.counter("price.aggregation.total");
    }

    // Note: @Timed annotations on methods will be handled by Micrometer's aspect
    // automatically
    // No need to manually create Timer beans
}
