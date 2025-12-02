package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import com.cryptoArb.crypto_price_aggregator.service.impl.PriceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriceServiceGetLatestTicksTest {

    private CurrencyPair btcUsd;
    private List<PriceFetcher> fetchers;
    private PriceTickRepository mockRepository;

    @BeforeEach
    void setUp() {
        btcUsd = CurrencyPair.of("BTC", "USD");
        fetchers = new ArrayList<>();
        mockRepository = mock(PriceTickRepository.class);
    }

    @Test
    @DisplayName("Should return map of latest ticks from all fetchers")
    void shouldReturnMapOfLatestTicks() throws Exception {
        // Arrange
        PriceFetcher binanceFetcher = mock(PriceFetcher.class);
        PriceFetcher coinbaseFetcher = mock(PriceFetcher.class);

        when(binanceFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.BINANCE, new BigDecimal("50000.00"), new BigDecimal("50100.00"), Instant.now()));
        when(binanceFetcher.getExchange()).thenReturn(Exchange.BINANCE);

        when(coinbaseFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.COINBASE, new BigDecimal("50050.00"), new BigDecimal("50150.00"), Instant.now()));
        when(coinbaseFetcher.getExchange()).thenReturn(Exchange.COINBASE);

        fetchers.add(binanceFetcher);
        fetchers.add(coinbaseFetcher);

        PriceService priceService = new PriceServiceImpl(fetchers, mockRepository);

        // Act
        Map<String, PriceTick> result = priceService.getLatestPriceTicks(btcUsd);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("BINANCE"));
        assertTrue(result.containsKey("COINBASE"));
        assertEquals(new BigDecimal("50000.00"), result.get("BINANCE").getBid());
        assertEquals(new BigDecimal("50050.00"), result.get("COINBASE").getBid());
    }

    @Test
    @DisplayName("Should return empty map when no fetchers available")
    void shouldReturnEmptyMapWhenNoFetchers() {
        // Arrange
        PriceService priceService = new PriceServiceImpl(new ArrayList<>(), mockRepository);

        // Act
        Map<String, PriceTick> result = priceService.getLatestPriceTicks(btcUsd);

        // Assert
        assertTrue(result.isEmpty());
    }
}
