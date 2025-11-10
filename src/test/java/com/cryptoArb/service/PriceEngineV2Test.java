package com.cryptoArb.service;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;


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

    private List<PriceFetcher> fetchers;
    private PriceEngineV2 priceEngineV2;

    // Helper to create dummy ticks
    private PriceTick tick1 = new PriceTick(
            new CurrencyPair("BTC", "USD"),
            new Exchange("mock1"),
            Instant.now(), BigDecimal.ONE, BigDecimal.TEN);
    private PriceTick tick2 = new PriceTick(new CurrencyPair("ETH", "USD"), new Exchange("mock2"), Instant.now(), BigDecimal.ONE, BigDecimal.TEN);

    @BeforeEach
    void setUp() throws PriceFetchException {
        // 1. Given: A list of mock fetchers
        fetchers = List.of(mockFetcher1, mockFetcher2);

        // Define the behavior of our mock fetchers
        Mockito.when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1));
        Mockito.when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2));

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
    void shouldRunAsyncCycleAndSaveTicks() {
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
}