package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.cryptoArb.crypto_price_aggregator.service.impl.PriceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriceServiceTest {

    private CurrencyPair btcUsd;
    private List<PriceFetcher> fetchers;

    @BeforeEach
    void setUp() {
        btcUsd = new CurrencyPair("BTC", "USD");
        fetchers = new ArrayList<>();
    }

    @Test
    @DisplayName("Should return empty when no fetchers are available")
    void shouldReturnEmptyWhenNoFetchers() {
        // Arrange
        PriceService priceService = new PriceServiceImpl(new ArrayList<>());

        // Act
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        assertTrue(result.isEmpty(), "Service should return empty when no fetchers available");
    }

    @Test
    @DisplayName("Should return AggregatedTopOfBookQuote from single fetcher")
    void shouldReturnAggregatedTopOfBookQuoteFromSingleFetcher() throws PriceFetchException {
        // Arrange
        PriceFetcher mockFetcher = mock(PriceFetcher.class);
        PriceTick tick = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000.00"), new BigDecimal("50100.00"), Instant.now());
        when(mockFetcher.fetchPrice(btcUsd)).thenReturn(tick);

        fetchers.add(mockFetcher);
        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        assertTrue(result.isPresent(), "Should return price from single fetcher");
        assertEquals(new BigDecimal("50000.00"), result.get().bestBid());
        assertEquals(new BigDecimal("50100.00"), result.get().bestAsk());
        assertEquals(btcUsd, result.get().pair());
    }

    @Test
    @DisplayName("Should aggregate prices correctly: max bestBid, min bestAsk")
    void shouldAggregatePricesCorrectly() throws PriceFetchException {
        // Arrange
        PriceFetcher fetcher1 = mock(PriceFetcher.class);
        PriceFetcher fetcher2 = mock(PriceFetcher.class);
        PriceFetcher fetcher3 = mock(PriceFetcher.class);

        // Fetcher1: bestBid=50000, bestAsk=50200
        when(fetcher1.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.BINANCE,
                        new BigDecimal("50000.00"), new BigDecimal("50200.00"), Instant.now()));

        // Fetcher2: bestBid=50100 (HIGHEST), bestAsk=50150 (LOWEST)
        when(fetcher2.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.COINBASE,
                        new BigDecimal("50100.00"), new BigDecimal("50150.00"), Instant.now()));

        // Fetcher3: bestBid=50050, bestAsk=50250
        when(fetcher3.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.KRAKEN,
                        new BigDecimal("50050.00"), new BigDecimal("50250.00"), Instant.now()));

        fetchers.add(fetcher1);
        fetchers.add(fetcher2);
        fetchers.add(fetcher3);
        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        assertTrue(result.isPresent(), "Should return aggregated price");
        assertEquals(new BigDecimal("50100.00"), result.get().bestBid(),
                "Should return max bestBid (from fetcher2)");
        assertEquals(new BigDecimal("50150.00"), result.get().bestAsk(),
                "Should return min bestAsk (from fetcher2)");
    }

    @Test
    @DisplayName("Should return empty when all fetchers fail")
    void shouldReturnEmptyWhenAllFetchersFail() throws PriceFetchException {
        // Arrange
        PriceFetcher fetcher1 = mock(PriceFetcher.class);
        PriceFetcher fetcher2 = mock(PriceFetcher.class);

        when(fetcher1.fetchPrice(btcUsd))
                .thenThrow(new PriceFetchException("Network error"));
        when(fetcher2.fetchPrice(btcUsd))
                .thenThrow(new PriceFetchException("API rate limit"));

        fetchers.add(fetcher1);
        fetchers.add(fetcher2);
        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when all fetchers fail");
    }

    @Test
    @DisplayName("Should aggregate from successful fetchers when some fail")
    void shouldAggregateFromSuccessfulFetchersWhenSomeFail() throws PriceFetchException {
        // Arrange
        PriceFetcher successFetcher = mock(PriceFetcher.class);
        PriceFetcher failFetcher = mock(PriceFetcher.class);

        when(successFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.BINANCE,
                        new BigDecimal("50000.00"), new BigDecimal("50100.00"), Instant.now()));
        when(failFetcher.fetchPrice(btcUsd))
                .thenThrow(new PriceFetchException("Temporary error"));

        fetchers.add(successFetcher);
        fetchers.add(failFetcher);
        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        assertTrue(result.isPresent(), "Should return price from successful fetcher");
        assertEquals(new BigDecimal("50000.00"), result.get().bestBid());
        assertEquals(new BigDecimal("50100.00"), result.get().bestAsk());
    }

    @Test
    @DisplayName("Should throw exception for null currency pair")
    void shouldThrowExceptionForNullPair() {
        // Arrange
        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            priceService.getAggregatedTopOfBookQuote(null);
        });

        assertTrue(exception.getMessage().contains("CurrencyPair cannot be null"));
    }

    @Test
    @DisplayName("Should handle null fetchers list gracefully")
    void shouldHandleNullFetchersListGracefully() {
        // Arrange
        PriceService priceService = new PriceServiceImpl(null);

        // Act
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when fetchers list is null");
    }

    @Test
    @DisplayName("Should call all fetchers sequentially")
    void shouldCallAllFetchersSequentially() throws PriceFetchException {
        // Arrange
        PriceFetcher fetcher1 = mock(PriceFetcher.class);
        PriceFetcher fetcher2 = mock(PriceFetcher.class);

        when(fetcher1.fetchPrice(any())).thenReturn(
                new PriceTick(btcUsd, Exchange.BINANCE,
                        new BigDecimal("50000.00"), new BigDecimal("50100.00"), Instant.now()));
        when(fetcher2.fetchPrice(any())).thenReturn(
                new PriceTick(btcUsd, Exchange.COINBASE,
                        new BigDecimal("50050.00"), new BigDecimal("50150.00"), Instant.now()));

        fetchers.add(fetcher1);
        fetchers.add(fetcher2);
        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act
        priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert
        verify(fetcher1, times(1)).fetchPrice(btcUsd);
        verify(fetcher2, times(1)).fetchPrice(btcUsd);
    }

    @Test
    @DisplayName("Phase 2 Cycle 3: Should aggregate prices from 4 mock exchanges correctly")
    void shouldAggregatePricesFrom4MockExchanges() throws PriceFetchException {
        // RED-GREEN: Test with 4 mock exchanges to prepare for parallel fetching
        // This test validates that our service can handle 4 exchanges
        // before we introduce concurrency in Cycle 4

        // Arrange: Create 4 mock fetchers representing different exchanges
        PriceFetcher binanceFetcher = mock(PriceFetcher.class);
        PriceFetcher coinbaseFetcher = mock(PriceFetcher.class);
        PriceFetcher krakenFetcher = mock(PriceFetcher.class);
        PriceFetcher mockFetcher = mock(PriceFetcher.class);

        // Binance: bid=50000, ask=50200
        when(binanceFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.BINANCE,
                        new BigDecimal("50000.00"), new BigDecimal("50200.00"), Instant.now()));
        when(binanceFetcher.getExchange()).thenReturn(Exchange.BINANCE);

        // Coinbase: bid=50100 (HIGHEST BID), ask=50150
        when(coinbaseFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.COINBASE,
                        new BigDecimal("50100.00"), new BigDecimal("50150.00"), Instant.now()));
        when(coinbaseFetcher.getExchange()).thenReturn(Exchange.COINBASE);

        // Kraken: bid=50050, ask=50100 (LOWEST ASK)
        when(krakenFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.KRAKEN,
                        new BigDecimal("50050.00"), new BigDecimal("50100.00"), Instant.now()));
        when(krakenFetcher.getExchange()).thenReturn(Exchange.KRAKEN);

        // Mock: bid=50075, ask=50175
        when(mockFetcher.fetchPrice(btcUsd)).thenReturn(
                new PriceTick(btcUsd, Exchange.MOCK,
                        new BigDecimal("50075.00"), new BigDecimal("50175.00"), Instant.now()));
        when(mockFetcher.getExchange()).thenReturn(Exchange.MOCK);

        // Add all 4 fetchers
        fetchers.add(binanceFetcher);
        fetchers.add(coinbaseFetcher);
        fetchers.add(krakenFetcher);
        fetchers.add(mockFetcher);

        PriceService priceService = new PriceServiceImpl(fetchers);

        // Act: Fetch aggregated quote
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert: Verify aggregation is correct
        assertTrue(result.isPresent(), "Should return aggregated price from 4 fetchers");

        // Best bid should be 50100 (from Coinbase)
        assertEquals(new BigDecimal("50100.00"), result.get().bestBid(),
                "Should return max bid from Coinbase");
        assertEquals(Exchange.COINBASE, result.get().bestBidExchange(),
                "Best bid should be from Coinbase");

        // Best ask should be 50100 (from Kraken)
        assertEquals(new BigDecimal("50100.00"), result.get().bestAsk(),
                "Should return min ask from Kraken");
        assertEquals(Exchange.KRAKEN, result.get().bestAskExchange(),
                "Best ask should be from Kraken");

        // Verify all 4 fetchers were called
        verify(binanceFetcher, times(1)).fetchPrice(btcUsd);
        verify(coinbaseFetcher, times(1)).fetchPrice(btcUsd);
        verify(krakenFetcher, times(1)).fetchPrice(btcUsd);
        verify(mockFetcher, times(1)).fetchPrice(btcUsd);
    }

    @Test
    @DisplayName("Should execute fetches in parallel (Total time < Sum of delays)")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void shouldExecuteFetchesInParallel() {
        // GIVEN: 3 fetchers with 500ms delay each
        // Sequential: 1500ms (Fail)
        // Parallel: ~500ms + overhead (Pass)
        long delayMs = 500;
        PriceFetcher f1 = createSlowFetcher(delayMs, Exchange.BINANCE);
        PriceFetcher f2 = createSlowFetcher(delayMs, Exchange.COINBASE);
        PriceFetcher f3 = createSlowFetcher(delayMs, Exchange.KRAKEN);

        fetchers.add(f1);
        fetchers.add(f2);
        fetchers.add(f3);

        PriceService priceService = new PriceServiceImpl(fetchers);

        // WHEN
        long start = System.nanoTime();
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // THEN
        assertTrue(result.isPresent(), "Should return a result");
        assertTrue(durationMs < (delayMs * 2),
                String.format("Should be parallel! Took %d ms, expected < %d ms", durationMs, delayMs * 2));
    }

    private PriceFetcher createSlowFetcher(long delayMs, Exchange exchange) {
        PriceFetcher fetcher = mock(PriceFetcher.class);
        try {
            when(fetcher.fetchPrice(any())).thenAnswer(inv -> {
                Thread.sleep(delayMs);
                return new PriceTick(btcUsd, exchange,
                        new BigDecimal("100"), new BigDecimal("101"), Instant.now());
            });
            when(fetcher.getExchange()).thenReturn(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return fetcher;
    }
}