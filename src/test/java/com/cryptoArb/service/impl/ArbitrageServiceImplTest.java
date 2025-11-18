package com.cryptoArb.service.impl;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.repository.ArbitrageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArbitrageServiceImplTest {

    @Mock
    private ArbitrageRepository arbitrageRepository;

    @InjectMocks
    private ArbitrageServiceImpl arbitrageService;

    // Reuse the same dummy opportunity for setup
    private final ArbitrageOpportunity dummyOpp = new ArbitrageOpportunity(
            new CurrencyPair("BTC", "USD"),
            Instant.now(),
            new Exchange("kraken"),
            new BigDecimal("50000"),
            new Exchange("binance"),
            new BigDecimal("50100"),
            new BigDecimal("0.002")
    );

    @Test
    @DisplayName("Default getRecentOpportunities should use 5 minutes")
    void shouldGetRecentOpportunitiesDefault5Minutes() {
        // --- Given ---
        Instant now = Instant.now();
        when(arbitrageRepository.findByTimestampAfter(any(Instant.class)))
                .thenReturn(List.of(dummyOpp));

        // --- When ---
        // Call the no-arg method (defaults)
        List<ArbitrageOpportunity> results = arbitrageService.getRecentOpportunities();

        // --- Then ---
        assertNotNull(results);
        assertEquals(1, results.size());

        // Verify timestamp was 5 minutes ago
        ArgumentCaptor<Instant> timestampCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(arbitrageRepository).findByTimestampAfter(timestampCaptor.capture());

        Instant capturedTime = timestampCaptor.getValue();
        Instant expectedTime = now.minus(5, ChronoUnit.MINUTES); // Changed from 24 hours

        long diffSeconds = Math.abs(ChronoUnit.SECONDS.between(expectedTime, capturedTime));
        assertEquals(0, diffSeconds, 1, "Should query for data after 5 minutes ago");
    }

    @Test
    @DisplayName("Parameterized getRecentOpportunities should use provided duration")
    void shouldGetRecentOpportunitiesWithCustomDuration() {
        // --- Given ---
        Instant now = Instant.now();
        Duration customDuration = Duration.ofHours(3); // Custom 3 hour window

        when(arbitrageRepository.findByTimestampAfter(any(Instant.class)))
                .thenReturn(List.of(dummyOpp));

        // --- When ---
        // Call the new parameterized method
        // (This method doesn't exist yet -> Compilation Error)
        List<ArbitrageOpportunity> results = arbitrageService.getRecentOpportunities(customDuration);

        // --- Then ---
        assertNotNull(results);

        ArgumentCaptor<Instant> timestampCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(arbitrageRepository).findByTimestampAfter(timestampCaptor.capture());

        Instant capturedTime = timestampCaptor.getValue();
        Instant expectedTime = now.minus(customDuration);

        long diffSeconds = Math.abs(ChronoUnit.SECONDS.between(expectedTime, capturedTime));
        assertEquals(0, diffSeconds, 1, "Should query for data after 3 hours ago");
    }
}