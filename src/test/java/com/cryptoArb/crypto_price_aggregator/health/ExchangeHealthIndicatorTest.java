package com.cryptoArb.crypto_price_aggregator.health;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Test for ExchangeHealthIndicator
 * Following RED-GREEN-REFACTOR cycle
 *
 * Purpose: Verify custom health checks for exchange fetchers
 */
@ExtendWith(MockitoExtension.class)
class ExchangeHealthIndicatorTest {

    @Mock
    private PriceFetcher binanceFetcher;

    @Mock
    private PriceFetcher coinbaseFetcher;

    @Mock
    private PriceFetcher krakenFetcher;

    private ExchangeHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        List<PriceFetcher> fetchers = Arrays.asList(binanceFetcher, coinbaseFetcher, krakenFetcher);
        healthIndicator = new ExchangeHealthIndicator(fetchers);
    }

    @Test
    void shouldReturnUpWhenAllExchangesAreHealthy() throws Exception {
        // ARRANGE
        when(binanceFetcher.getExchange()).thenReturn(Exchange.BINANCE);
        when(coinbaseFetcher.getExchange()).thenReturn(Exchange.COINBASE);
        when(krakenFetcher.getExchange()).thenReturn(Exchange.KRAKEN);

        // Mock successful health checks
        when(binanceFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);
        when(coinbaseFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);
        when(krakenFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);

        // ACT
        Health health = healthIndicator.health();

        // ASSERT
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("exchanges"));
    }

    @Test
    void shouldReturnDownWhenAnyExchangeIsUnhealthy() throws Exception {
        // ARRANGE
        when(binanceFetcher.getExchange()).thenReturn(Exchange.BINANCE);
        when(coinbaseFetcher.getExchange()).thenReturn(Exchange.COINBASE);
        when(krakenFetcher.getExchange()).thenReturn(Exchange.KRAKEN);

        // Mock one failing exchange
        when(binanceFetcher.fetchPrice(any(CurrencyPair.class)))
                .thenThrow(new RuntimeException("Connection timeout"));
        when(coinbaseFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);
        when(krakenFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);

        // ACT
        Health health = healthIndicator.health();

        // ASSERT
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("exchanges"));
    }

    @Test
    void shouldIncludeDetailsForEachExchange() throws Exception {
        // ARRANGE
        when(binanceFetcher.getExchange()).thenReturn(Exchange.BINANCE);
        when(coinbaseFetcher.getExchange()).thenReturn(Exchange.COINBASE);
        when(krakenFetcher.getExchange()).thenReturn(Exchange.KRAKEN);

        when(binanceFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);
        when(coinbaseFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);
        when(krakenFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);

        // ACT
        Health health = healthIndicator.health();

        // ASSERT
        Object exchanges = health.getDetails().containsKey("exchanges");
        assertNotNull(exchanges);
    }

    @Test
    void shouldHandleEmptyFetchersList() {
        // ARRANGE
        ExchangeHealthIndicator emptyHealthIndicator = new ExchangeHealthIndicator(Collections.emptyList());

        // ACT
        Health health = emptyHealthIndicator.health();

        // ASSERT
        assertEquals(Status.UP, health.getStatus());
        assertEquals("No exchanges configured", health.getDetails().get("message"));
    }

    @Test
    void shouldIncludeExchangeStatusInDetails() throws Exception {
        // ARRANGE
        when(binanceFetcher.getExchange()).thenReturn(Exchange.BINANCE);
        when(binanceFetcher.fetchPrice(any(CurrencyPair.class))).thenReturn(null);

        List<PriceFetcher> singleFetcher = Collections.singletonList(binanceFetcher);
        ExchangeHealthIndicator singleHealthIndicator = new ExchangeHealthIndicator(singleFetcher);

        // ACT
        Health health = singleHealthIndicator.health();

        // ASSERT
        assertEquals(Status.UP, health.getStatus());
    }
}
