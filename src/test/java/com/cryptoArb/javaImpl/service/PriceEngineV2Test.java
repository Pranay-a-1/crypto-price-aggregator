package com.cryptoArb.javaImpl.service;

import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import com.cryptoArb.javaImpl.domain_records.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;




// Use Mockito extension to automatically initialize mocks
@ExtendWith(MockitoExtension.class)
class PriceEngineV2Test {

    // 1. Mock all dependencies
    @Mock
    private DatabaseService mockDbService;
    @Mock
    private PriceService mockPriceService;
    @Mock
    private ArbitrageService mockArbitrageService;
    @Mock
    private OpportunityAggregator mockOppAggregator;

    @Mock
    private PriceFetcher mockFetcher1;
    @Mock
    private PriceFetcher mockFetcher2;
    @Mock
    private PriceFetcher mockFetcher3_Fails;

    private List<PriceFetcher> fetchers;
    private PriceEngineV2 priceEngineV2;

    // Helper to create dummy ticks
    private PriceTick tick1 = new PriceTick(
            new CurrencyPair("BTC", "USD"),
            new Exchange("mock1"),
            Instant.now(), BigDecimal.ONE, BigDecimal.TEN);
    private PriceTick tick2 = new PriceTick(new CurrencyPair("ETH", "USD"), new Exchange("mock2"), Instant.now(), BigDecimal.ONE, BigDecimal.TEN);


    // --- Mocks for the chain ---
    private Map<CurrencyPair, ConsolidatedPrice> mockPriceMap = Map.of(
            new CurrencyPair("BTC", "USD"), new ConsolidatedPrice(null, null, null, null, null, null)
    );
    // We must initialize a *valid* object that passes constructor validation.
    private List<ArbitrageOpportunity> mockOpportunities = List.of(
            new ArbitrageOpportunity.Builder()
                    .pair(new CurrencyPair("DUMMY", "DUMMY"))
                    .timestamp(Instant.now())
                    .buyExchange(new Exchange("dummy-buy"))
                    .buyPrice(BigDecimal.ONE)
                    .sellExchange(new Exchange("dummy-sell"))
                    .sellPrice(BigDecimal.TEN)
                    .build() // This now passes validation
    );


    @BeforeEach
    void setUp() throws PriceFetchException {
        // 1. Given: A list of mock fetchers
        fetchers = List.of(mockFetcher1, mockFetcher2);


        // 2. When: We create the engine with all mocked services
        priceEngineV2 = new PriceEngineV2(
                fetchers,
                mockDbService,
                mockPriceService,
                mockArbitrageService,
                mockOppAggregator
        );
    }

    @AfterEach
    void tearDown() {
        priceEngineV2.stop();
    }

    // --- THIS IS THE UPDATED TEST ---
    @Test
    @DisplayName("Should run async cycle and save all ticks")
    void shouldRunAsyncCycleAndSaveTicks() throws PriceFetchException{
        // Configure mock fetchers
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1));
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2));


        // When: We run the fetch cycle
        priceEngineV2.runFetchCycle();

        // Then: We use Awaitility to wait (at most 2 seconds)
        // for our async assertions to pass.
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            // We verify that our mock database service was called
            // with tick1 and tick2.
            verify(mockDbService).saveTick(tick1);
            verify(mockDbService).saveTick(tick2);
        });
    }


    @Test
    @DisplayName("Should run full async pipeline (fetch, save, aggregate, analyze, collect)")
    void shouldRunFullAsyncPipeline() throws PriceFetchException {
        // --- Given ---
        // Configure mock fetchers
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1));
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2));

        // Configure the rest of the mock chain
        List<PriceTick> allTicks = List.of(tick1, tick2);
        // We use 'any' for the list because the stream collectors might create a new list instance
        // We use argThat to match by content, not instance
        when(mockPriceService.aggregatePrices(argThat(list ->
                list.size() == 2 && list.containsAll(List.of(tick1, tick2))
        ))).thenReturn(mockPriceMap);
        when(mockArbitrageService.findArbitrageOpportunities(mockPriceMap)).thenReturn(mockOpportunities);

        // --- When ---
        // The existing implementation will NOT call the full chain,
        // so this test will fail.
        CompletableFuture<Void> cycle = priceEngineV2.runFetchCycle();

        // --- Then ---
        // We wait for the *entire* chain to complete.
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(cycle::isDone);

        // Verify the full chain was called
        // 1. Save ticks
        verify(mockDbService).saveTick(tick1);
        verify(mockDbService).saveTick(tick2);
        // 2. Aggregate ticks (verify it was called with a list of size 2)
        verify(mockPriceService).aggregatePrices(argThat(list -> list.size() == 2 && list.contains(tick1) && list.contains(tick2)));
        // 3. Find arbitrage
        verify(mockArbitrageService).findArbitrageOpportunities(mockPriceMap);
        // 4. Collect opportunities
        verify(mockOppAggregator).addOpportunity(any(ArbitrageOpportunity.class));
    }


    @Test
    @DisplayName("Should handle a fetcher failure gracefully with .exceptionally()")
    void shouldHandleFetcherFailure() throws PriceFetchException {
        // --- Given ---
        // A different set of fetchers for this test
        fetchers = List.of(mockFetcher1, mockFetcher3_Fails);
        priceEngineV2 = new PriceEngineV2(
                fetchers, mockDbService, mockPriceService,
                mockArbitrageService, mockOppAggregator
        );

        // Fetcher 1 SUCCEEDS
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1));
        // Fetcher 3 FAILS
        when(mockFetcher3_Fails.fetchPrices()).thenThrow(new PriceFetchException("Network error"));
        when(mockFetcher3_Fails.getExchangeName()).thenReturn("FailingFetcher");

        // The rest of the chain should still run, but *only* with tick1
        List<PriceTick> allTicks = List.of(tick1); // Note: tick2 is missing
        when(mockPriceService.aggregatePrices(allTicks)).thenReturn(mockPriceMap);
        when(mockArbitrageService.findArbitrageOpportunities(mockPriceMap)).thenReturn(mockOpportunities);

        // --- When ---
        CompletableFuture<Void> cycle = priceEngineV2.runFetchCycle();

        // --- Then ---
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(cycle::isDone);

        // Verify the chain was called, but only with the good data
        // 1. Save ticks (only tick1)
        verify(mockDbService).saveTick(tick1);
        verify(mockDbService, never()).saveTick(tick2);
        // 2. Aggregate ticks (only tick1)
        verify(mockPriceService).aggregatePrices(allTicks);
        // 3. Find arbitrage
        verify(mockArbitrageService).findArbitrageOpportunities(mockPriceMap);
        // 4. Collect opportunities
        verify(mockOppAggregator).addOpportunity(any(ArbitrageOpportunity.class));
    }
}