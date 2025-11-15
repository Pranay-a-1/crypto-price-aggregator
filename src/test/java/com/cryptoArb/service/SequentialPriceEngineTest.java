package com.cryptoArb.service;

import com.cryptoArb.domain_records.CurrencyPair;
import com.cryptoArb.domain_records.Exchange;
import com.cryptoArb.domain_records.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;


/**
 * Tests for the SequentialPriceEngine.
 * We use Mockito to simulate the behavior of its dependencies.
 */
@ExtendWith(MockitoExtension.class)
class SequentialPriceEngineTest {

    // --- Mocks ---
    @Mock
    private DatabaseService mockDatabaseService;

    @Mock
    private PriceFetcher mockFetcher1; // "Binance"

    @Mock
    private PriceFetcher mockFetcher2; // "Coinbase"

    // --- System Under Test (SUT) ---
    private SequentialPriceEngine engine;

    @BeforeEach
    void setUp() {
        // --- GIVEN (Mock Names) ---
        // This helps with logging and debugging
        when(mockFetcher1.getExchangeName()).thenReturn("Binance");
        when(mockFetcher2.getExchangeName()).thenReturn("Coinbase");

        // Create a new engine before each test, injecting our mocks
        engine = new SequentialPriceEngine(
                List.of(mockFetcher1, mockFetcher2),
                mockDatabaseService
        );
    }

    @Test
    void testRunFetchCycle_FetchesAndSavesAllTicks() throws Exception { // <-- Added throws
        // --- Given (Our Setup) ---

        // 1. Define the domain objects our constructor needs
        Exchange exBinance = new Exchange("Binance");
        Exchange exCoinbase = new Exchange("Coinbase");
        CurrencyPair pairBtcUsd = new CurrencyPair("BTC" , "USD");
        CurrencyPair pairEthUsd = new CurrencyPair("ETH" , "USD");
        Instant now = Instant.now();

        // 2. Define the data our mocks will return (using the *correct* constructor)
        // We assume the two BigDecimals are bid and ask.
        PriceTick tick1_1 = new PriceTick(pairBtcUsd, exBinance, now, new BigDecimal("30000"), new BigDecimal("30001"));
        PriceTick tick1_2 = new PriceTick(pairEthUsd, exBinance, now, new BigDecimal("2000"), new BigDecimal("2000.50"));
        PriceTick tick2_1 = new PriceTick(pairBtcUsd, exCoinbase, now, new BigDecimal("30002"), new BigDecimal("30003"));

        // 3. "Program" the mocks with their behavior
        // When mockFetcher1.fetchPrices() is called, return these two ticks.
        // We add "throws Exception" because fetchPrices() is a checked exception
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1_1, tick1_2));

        // When mockFetcher2.fetchPrices() is called, return this one tick.
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2_1));

        // --- When (The Action) ---
        // We call the method we are testing
        engine.runFetchCycle();

        // --- Then (The Verification) ---
        // We verify that the *correct actions occurred*.

        // 1. Check that fetchPrices() was called on *both* fetchers.
        verify(mockFetcher1, times(1)).fetchPrices();
        verify(mockFetcher2, times(1)).fetchPrices();

        // 2. Check that saveTick() was called for *all three* ticks.
        verify(mockDatabaseService, times(1)).saveTick(tick1_1);
        verify(mockDatabaseService, times(1)).saveTick(tick1_2);
        verify(mockDatabaseService, times(1)).saveTick(tick2_1);

        // 3. A more robust check: verify *exactly* 3 calls to saveTick,
        //    and no other interactions with this mock.
        verify(mockDatabaseService, times(3)).saveTick(any(PriceTick.class));
        verifyNoMoreInteractions(mockDatabaseService);
    }


    @Test
    void testRunFetchCycle_ContinuesWhenOneFetcherFails() throws Exception {
        // --- Given (Our Setup) ---
        // 1. Define the domain objects our constructor needs
        Exchange exCoinbase = new Exchange("Coinbase");
        CurrencyPair pairBtcUsd = new CurrencyPair("BTC", "USD");
        Instant now = Instant.now();

        // 2. Define data for the *successful* fetcher
        PriceTick tick2_1 = new PriceTick(pairBtcUsd, exCoinbase, now, new BigDecimal("30002"), new BigDecimal("30003"));

        // 3. "Program" the mocks with their behavior

        // ** THE KEY DIFFERENCE **
        // When mockFetcher1.fetchPrices() is called, throw an exception.
        when(mockFetcher1.fetchPrices())
                .thenThrow(new PriceFetchException("API is down"));

        // When mockFetcher2.fetchPrices() is called, return its tick.
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2_1));

        // --- When (The Action) ---
        // We call the method we are testing
        engine.runFetchCycle();

        // --- Then (The Verification) ---
        // We verify the *correct* robust behavior.

        // 1. Check that fetchPrices() was still called on *both* fetchers.
        verify(mockFetcher1, times(1)).fetchPrices();
        verify(mockFetcher2, times(1)).fetchPrices();

        // 2. Check that saveTick() was called *only* for the successful tick.
        verify(mockDatabaseService, times(1)).saveTick(tick2_1);

        // 3. A more robust check: verify *exactly* 1 call to saveTick.
        // This proves we didn't save any "ghost" ticks from the failed fetcher.
        verify(mockDatabaseService, times(1)).saveTick(any(PriceTick.class));
        verifyNoMoreInteractions(mockDatabaseService);
    }
}