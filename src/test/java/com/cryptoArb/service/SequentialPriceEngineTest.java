package com.cryptoArb.service;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import com.cryptoArb.repository.PriceTickRepository;
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

    // THIS IS THE CHANGE: We no longer mock DatabaseService
    // @Mock
    // private DatabaseService mockDatabaseService;

    // INSTEAD: We mock our new repository
    @Mock
    private PriceTickRepository mockPriceTickRepository;

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

        // THIS LINE WILL FAIL TO COMPILE (RED)
        // The constructor expects "mockDatabaseService",
        // but we now have "mockPriceTickRepository".
        engine = new SequentialPriceEngine(
                List.of(mockFetcher1, mockFetcher2),
                mockPriceTickRepository
        );
    }

    @Test
    void testRunFetchCycle_FetchesAndSavesAllTicks() throws Exception { // <-- Added throws
        // --- Given (Our Setup) ---

        // 1. Define the domain objects (no change here)
        Exchange exBinance = new Exchange("Binance");
        Exchange exCoinbase = new Exchange("Coinbase");
        CurrencyPair pairBtcUsd = new CurrencyPair("BTC" , "USD");
        CurrencyPair pairEthUsd = new CurrencyPair("ETH" , "USD");
        Instant now = Instant.now();

        // 2. Define the data our mocks will return (no change here)
        PriceTick tick1_1 = new PriceTick(pairBtcUsd, exBinance, now, new BigDecimal("30000"), new BigDecimal("30001"));
        PriceTick tick1_2 = new PriceTick(pairEthUsd, exBinance, now, new BigDecimal("2000"), new BigDecimal("2000.50"));
        PriceTick tick2_1 = new PriceTick(pairBtcUsd, exCoinbase, now, new BigDecimal("30002"), new BigDecimal("30003"));

        // 3. "Program" the mocks (no change here)
        when(mockFetcher1.fetchPrices()).thenReturn(List.of(tick1_1, tick1_2));
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2_1));

        // --- When (The Action) ---
        engine.runFetchCycle();

        // --- Then (The Verification) ---

        // 1. Check that fetchPrices() was called on *both* fetchers.
        verify(mockFetcher1, times(1)).fetchPrices();
        verify(mockFetcher2, times(1)).fetchPrices();

        // 2. THIS IS THE NEW VERIFICATION:
        // We verify that our *repository's* save method was called.
        // We use any() because we will need to map the PriceTick record
        // to a PriceTick_spring entity, which we haven't done yet.
        verify(mockPriceTickRepository, times(3)).save(any(PriceTick.class));
        verifyNoMoreInteractions(mockPriceTickRepository);
    }


    @Test
    void testRunFetchCycle_ContinuesWhenOneFetcherFails() throws Exception {
        // --- Given (Our Setup) ---
        // 1. Define domain objects (no change)
        Exchange exCoinbase = new Exchange("Coinbase");
        CurrencyPair pairBtcUsd = new CurrencyPair("BTC", "USD");
        Instant now = Instant.now();

        // 2. Define data for the *successful* fetcher (no change)
        PriceTick tick2_1 = new PriceTick(pairBtcUsd, exCoinbase, now, new BigDecimal("30002"), new BigDecimal("30003"));

        // 3. "Program" the mocks (no change)
        when(mockFetcher1.fetchPrices())
                .thenThrow(new PriceFetchException("API is down"));
        when(mockFetcher2.fetchPrices()).thenReturn(List.of(tick2_1));

        // --- When (The Action) ---
        engine.runFetchCycle();

        // --- Then (The Verification) ---

        // 1. Check that fetchPrices() was still called on *both* fetchers.
        verify(mockFetcher1, times(1)).fetchPrices();
        verify(mockFetcher2, times(1)).fetchPrices();

        // 2. THIS IS THE NEW VERIFICATION:
        // Check that save() was called *only* for the successful tick.
        verify(mockPriceTickRepository, times(1)).save(any(PriceTick.class));
        verifyNoMoreInteractions(mockPriceTickRepository);
    }
}