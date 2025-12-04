package com.cryptoArb.crypto_price_aggregator.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Test for MetricsConfig
 * Following RED-GREEN-REFACTOR cycle
 *
 * Purpose: Verify custom metrics are registered
 */
@ExtendWith(MockitoExtension.class)
class MetricsConfigTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter mockCounter;

    @Test
    void shouldRegisterPriceFetchSuccessCounter() {
        // ARRANGE - Will FAIL because MetricsConfig doesn't exist
        when(meterRegistry.counter(eq("price.fetch.success"), anyString(), anyString()))
                .thenReturn(mockCounter);

        // ACT
        MetricsConfig metricsConfig = new MetricsConfig();
        Counter counter = metricsConfig.priceFetchSuccessCounter(meterRegistry, "Binance");

        // ASSERT
        assertNotNull(counter);
        verify(meterRegistry).counter("price.fetch.success", "exchange", "Binance");
    }

    @Test
    void shouldRegisterPriceFetchFailureCounter() {
        // ARRANGE
        when(meterRegistry.counter(eq("price.fetch.failure"), anyString(), anyString()))
                .thenReturn(mockCounter);

        // ACT
        MetricsConfig metricsConfig = new MetricsConfig();
        Counter counter = metricsConfig.priceFetchFailureCounter(meterRegistry, "Binance");

        // ASSERT
        assertNotNull(counter);
        verify(meterRegistry).counter("price.fetch.failure", "exchange", "Binance");
    }

    @Test
    void shouldRegisterPriceAggregationCounter() {
        // ARRANGE
        when(meterRegistry.counter("price.aggregation.total"))
                .thenReturn(mockCounter);

        // ACT
        MetricsConfig metricsConfig = new MetricsConfig();
        Counter counter = metricsConfig.priceAggregationCounter(meterRegistry);

        // ASSERT
        assertNotNull(counter);
        verify(meterRegistry).counter("price.aggregation.total");
    }
}
