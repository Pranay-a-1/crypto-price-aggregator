package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ManualResilientBinanceFetcherTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private JsonNode jsonNode;

    private ManualResilientBinanceFetcher fetcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        fetcher = new ManualResilientBinanceFetcher(restTemplate, objectMapper, eventPublisher);

        // Disable chaos for unit tests by setting failure rate to 0 and latency to 0
        ReflectionTestUtils.setField(fetcher, "chaosFailureRate", 0);
        ReflectionTestUtils.setField(fetcher, "chaosLatencyMin", 0L);

        // Mock successful JSON parsing by default
        when(jsonNode.get("bidPrice")).thenReturn(mock(JsonNode.class));
        when(jsonNode.get("bidPrice").asText()).thenReturn("50000.00");
        when(jsonNode.get("askPrice")).thenReturn(mock(JsonNode.class));
        when(jsonNode.get("askPrice").asText()).thenReturn("50100.00");
    }

    @Test
    void fetchPrice_EventuallySucceeds_EvenWithChaos() throws Exception {
        // Given:
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        // When
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        PriceTick tick = fetcher.fetchPrice(pair);

        // Then
        assertNotNull(tick);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void fetchPrice_RetriesOnExternalFailure() throws Exception {
        // Given
        // We want to simulate RestTemplate failing 2 times then succeeding.

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("External API Down"))
                .thenThrow(new RuntimeException("External API Down"))
                .thenReturn("{}");

        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        CurrencyPair pair = new CurrencyPair("ETH", "USD");

        // When
        PriceTick tick = fetcher.fetchPrice(pair);

        // Then
        // Verify that restTemplate was called 3 times (2 failures + 1 success)
        verify(restTemplate, times(3)).getForObject(anyString(), eq(String.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }
}
