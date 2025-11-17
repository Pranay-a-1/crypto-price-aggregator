package com.cryptoArb.javaImpl.service_javaImpl;

import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.javaImpl.domain_records.CurrencyPair;
import com.cryptoArb.javaImpl.domain_records.Exchange;
import com.cryptoArb.javaImpl.domain_records.PriceTick;
import com.cryptoArb.javaImpl.fetcher_javaImpl.PriceFetcher;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PriceEngineV1Test {

    private PriceFetcher mockFetcher1;
    private PriceFetcher mockFetcher2;
    private List<PriceFetcher> fetchers;
    private BlockingQueue<PriceTick> tickQueue;
    private PriceEngineV1 priceEngine;

    private DatabaseService mockDbService;

    // Helper to create dummy ticks
    private PriceTick tick1 = new PriceTick(new CurrencyPair("BTC", "USD"), new Exchange("mock1"), Instant.now(), BigDecimal.ONE, BigDecimal.TEN);
    private PriceTick tick2 = new PriceTick(new CurrencyPair("ETH", "USD"), new Exchange("mock2"), Instant.now(), BigDecimal.ONE, BigDecimal.TEN);

    @BeforeEach
    void setUp() throws PriceFetchException {
        // 1. Given: A queue and a list of mock fetchers
        tickQueue = new LinkedBlockingQueue<>();

        // Mock fetcher 1
        // this mocks the PriceFetcher interface, like CoinbaseFetcher or BinanceFetcher
        // so that we can control its behavior in the test
        // when fetchPrices() is called, it will return a list with tick1
        mockFetcher1 = Mockito.mock(PriceFetcher.class);
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1));

        // Mock fetcher 2
        mockFetcher2 = Mockito.mock(PriceFetcher.class);
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2));

        fetchers = List.of(mockFetcher1, mockFetcher2);
        mockDbService = Mockito.mock(DatabaseService.class);

        // 2. When: We create the engine
        priceEngine = new PriceEngineV1(fetchers, tickQueue  , mockDbService);
    }

    @AfterEach
    void tearDown() {
        priceEngine.stop();
    }

    @Test
    @DisplayName("Should run fetch cycle, add ticks, and consume them")
    void shouldStartEngineAndProcessTicks() {
        // When: We start the entire engine
        priceEngine.start();

        // Then: The producer should run at least once (due to 0 initial delay)
        // and the consumer should process the ticks.

        // We wait for tick1 to be saved
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockDbService, atLeastOnce()).saveTick(tick1);
        });

        // We wait for tick2 to be saved
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockDbService, atLeastOnce()).saveTick(tick2);
        });
    }

    // We can delete the old 'shouldRunFetchersAndFillQueue' and
    // 'shouldConsumeTicksAndSaveToDb' tests, as the test above
    // covers both producer and consumer logic in an integrated way.
    // Or, we can keep them and modify them. Let's modify 'runFetchCycle'
    // to be package-private so we can still test it. (Done in the code)

    @Test
    @DisplayName("runFetchCycle should add all ticks to the queue")
    void runFetchCycle_shouldAddTicksToQueue() {
        // When
        priceEngine.runFetchCycle(); // This still works (package-private)

        // Then
        Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> tickQueue.size() == 2);

        assertEquals(2, tickQueue.size(), "Queue should have two ticks");
        assertTrue(tickQueue.contains(tick1), "Queue should contain tick from fetcher 1");
        assertTrue(tickQueue.contains(tick2), "Queue should contain tick from fetcher 2");
    }

    @Test
    @DisplayName("Consumer should take ticks from queue and save them")
    void consumer_shouldSaveTicks() throws InterruptedException {
        // When: We start *only* the consumer
        priceEngine.startConsumer(); // Was priceEngine.start()

        // And a tick is added manually
        tickQueue.put(tick1);

        // Then
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockDbService, Mockito.times(1)).saveTick(tick1);
        });
    }
}