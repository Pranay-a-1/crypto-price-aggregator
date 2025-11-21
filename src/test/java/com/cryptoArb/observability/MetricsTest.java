package com.cryptoArb.observability;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.PriceService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for Custom Metrics.
 *
 * This test verifies that the @Timed annotation is working correctly
 * and that custom metrics are being registered in the MeterRegistry.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for easier testing of metrics
class MetricsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private PriceService priceService;

    @MockitoBean
    private ArbitrageService arbitrageService;

    @Test
    @DisplayName("Should record custom @Timed metric for price endpoint")
    void shouldRecordTimedMetricForPriceEndpoint() throws Exception {
        // --- Given ---
        // Mock the service to return successfully
        when(priceService.getConsolidatedPriceForPair(any(CurrencyPair.class)))
                .thenReturn(Optional.of(new ConsolidatedPrice(
                        new CurrencyPair("BTC", "USD"),
                        java.time.Instant.now(),
                        java.math.BigDecimal.TEN,
                        null,
                        java.math.BigDecimal.ONE,
                        null
                )));

        // --- When ---
        // We call the endpoint multiple times
        mockMvc.perform(get("/api/v1/price/BTC-USD")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/price/BTC-USD")).andExpect(status().isOk());

        // --- Then ---
        // We verify that the 'cpa.price.request' timer exists and has recorded 2 counts.
        // NOTE: This assertion fails if we haven't configured the TimedAspect bean
        // or added the @Timed annotation.
        Timer timer = meterRegistry.find("cpa.price.request").timer();

        assertThat(timer)
                .overridingErrorMessage("Expected metric 'cpa.price.request' not found in registry")
                .isNotNull();

        assertThat(timer.count())
                .overridingErrorMessage("Expected timer to record 2 calls")
                .isEqualTo(2);


    }
}