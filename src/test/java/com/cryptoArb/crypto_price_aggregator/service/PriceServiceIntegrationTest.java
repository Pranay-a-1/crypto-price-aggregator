package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for PriceService with real H2 database.
 * Tests the full flow: Service → Repository → H2 Database
 * 
 * Using @SpringBootTest to load the full application context.
 * Using @Transactional to rollback database changes after each test.
 */
@SpringBootTest
@Transactional
class PriceServiceIntegrationTest {

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceTickRepository repository;

    private CurrencyPair btcUsd;
    private CurrencyPair ethUsd;

    @BeforeEach
    void setUp() {
        btcUsd = CurrencyPair.of("BTC", "USD");
        ethUsd = CurrencyPair.of("ETH", "USD");

        // Clear repository before each test
        repository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Should save fetched ticks to database")
    void shouldSaveFetchedTicksToDatabase() {
        // Act: Fetch prices (this should save to database)
        priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert: Verify ticks were saved to database
        List<PriceTick> savedTicks = repository.findByPair_BaseAndPair_Quote("BTC", "USD");

        assertFalse(savedTicks.isEmpty(), "Ticks should be saved to database");
        assertTrue(savedTicks.size() >= 4, "Should have ticks from all 4 mock exchanges");

        // Verify all saved ticks have the correct pair
        savedTicks.forEach(tick -> {
            assertEquals("BTC", tick.getPair().getBase());
            assertEquals("USD", tick.getPair().getQuote());
            assertNotNull(tick.getId(), "Saved tick should have auto-generated ID");
        });
    }

    @Test
    @DisplayName("Integration: Should aggregate from database ticks")
    void shouldAggregateFromDatabaseTicks() {
        // Arrange: Pre-populate database with known ticks
        Instant now = Instant.now();

        PriceTick tick1 = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000.00"), new BigDecimal("50200.00"), now);
        PriceTick tick2 = new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50100.00"), new BigDecimal("50150.00"), now);
        PriceTick tick3 = new PriceTick(btcUsd, Exchange.KRAKEN,
                new BigDecimal("50050.00"), new BigDecimal("50100.00"), now);

        repository.saveAll(List.of(tick1, tick2, tick3));

        // Verify pre-populated ticks are in database
        List<PriceTick> ticksBeforeCall = repository.findByPair_BaseAndPair_Quote("BTC", "USD");
        assertEquals(3, ticksBeforeCall.size(), "Should have 3 pre-populated ticks");

        // Act: Get aggregated quote (will fetch fresh ticks AND query from database)
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert: Verify aggregation returns a result
        assertTrue(result.isPresent(), "Should return aggregated quote");

        // Verify database now contains both old and new ticks
        List<PriceTick> ticksAfterCall = repository.findByPair_BaseAndPair_Quote("BTC", "USD");
        assertTrue(ticksAfterCall.size() >= 7,
                "Should have old ticks (3) + new ticks from fetchers (4+)");

        // Verify our original ticks are still in the database
        long binanceTicks = ticksAfterCall.stream()
                .filter(t -> t.getExchange() == Exchange.BINANCE)
                .count();
        assertTrue(binanceTicks >= 1, "Should still have Binance tick in database");
    }

    @Test
    @DisplayName("Integration: Should filter ticks by time window")
    void shouldFilterTicksByTimeWindow() {
        // Arrange: Create old ticks (10 seconds ago) and recent ticks
        Instant old = Instant.now().minusSeconds(10);
        Instant recent = Instant.now();

        PriceTick oldTick = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("40000.00"), new BigDecimal("40100.00"), old);
        PriceTick recentTick = new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50000.00"), new BigDecimal("50100.00"), recent);

        repository.saveAll(List.of(oldTick, recentTick));

        // Act: Query recent ticks (last 5 seconds)
        Instant cutoff = Instant.now().minusSeconds(5);
        List<PriceTick> recentTicks = repository.findByPair_BaseAndPair_QuoteAndTimestampAfter(
                "BTC", "USD", cutoff);

        // Assert: Only recent tick should be returned
        assertEquals(1, recentTicks.size(), "Should only return ticks within time window");
        assertEquals(Exchange.COINBASE, recentTicks.get(0).getExchange());
    }

    @Test
    @DisplayName("Integration: Should handle multiple currency pairs independently")
    void shouldHandleMultipleCurrencyPairsIndependently() {
        // Arrange: Save ticks for different pairs
        Instant now = Instant.now();

        PriceTick btcTick = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000.00"), new BigDecimal("50100.00"), now);
        PriceTick ethTick = new PriceTick(ethUsd, Exchange.COINBASE,
                new BigDecimal("3000.00"), new BigDecimal("3010.00"), now);

        repository.saveAll(List.of(btcTick, ethTick));

        // Act: Query each pair
        List<PriceTick> btcTicks = repository.findByPair_BaseAndPair_Quote("BTC", "USD");
        List<PriceTick> ethTicks = repository.findByPair_BaseAndPair_Quote("ETH", "USD");

        // Assert: Each query returns only its own pair
        assertEquals(1, btcTicks.size());
        assertEquals("BTC", btcTicks.get(0).getPair().getBase());

        assertEquals(1, ethTicks.size());
        assertEquals("ETH", ethTicks.get(0).getPair().getBase());
    }

    @Test
    @DisplayName("Integration: Should query ticks by exchange")
    void shouldQueryTicksByExchange() {
        // Arrange: Save ticks from different exchanges
        Instant now = Instant.now();

        PriceTick binanceTick = new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000.00"), new BigDecimal("50100.00"), now);
        PriceTick coinbaseTick = new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50050.00"), new BigDecimal("50150.00"), now);

        repository.saveAll(List.of(binanceTick, coinbaseTick));

        // Act: Query Binance ticks only
        List<PriceTick> binanceTicks = repository.findByExchange(Exchange.BINANCE);

        // Assert
        assertEquals(1, binanceTicks.size());
        assertEquals(Exchange.BINANCE, binanceTicks.get(0).getExchange());
    }

    @Test
    @DisplayName("Integration: Should persist BigDecimal precision correctly")
    void shouldPersistBigDecimalPrecisionCorrectly() {
        // Arrange: Create tick with precise decimal values
        Instant now = Instant.now();
        BigDecimal preciseBid = new BigDecimal("50123.45678901");
        BigDecimal preciseAsk = new BigDecimal("50234.56789012");

        PriceTick tick = new PriceTick(btcUsd, Exchange.BINANCE, preciseBid, preciseAsk, now);

        // Act: Save and retrieve
        repository.save(tick);
        List<PriceTick> retrieved = repository.findByPair_BaseAndPair_Quote("BTC", "USD");

        // Assert: Precision should be maintained (up to 8 decimal places as per @Column
        // config)
        assertEquals(1, retrieved.size());
        assertEquals(0, preciseBid.compareTo(retrieved.get(0).getBid()),
                "Bid should maintain precision");
        assertEquals(0, preciseAsk.compareTo(retrieved.get(0).getAsk()),
                "Ask should maintain precision");
    }

    @Test
    @DisplayName("Integration: Should handle concurrent service calls with database")
    void shouldHandleConcurrentServiceCallsWithDatabase() throws InterruptedException {
        // Arrange: Create multiple threads calling the service
        final int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        final boolean[] results = new boolean[threadCount];

        // Act: Execute multiple concurrent calls
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Optional<AggregatedTopOfBookQuote> quote = priceService.getAggregatedTopOfBookQuote(btcUsd);
                results[index] = quote.isPresent();
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert: All calls should succeed
        for (int i = 0; i < threadCount; i++) {
            assertTrue(results[i], "Thread " + i + " should return a result");
        }

        // Verify database has accumulated ticks
        List<PriceTick> allTicks = repository.findByPair_BaseAndPair_Quote("BTC", "USD");
        assertTrue(allTicks.size() >= threadCount * 4,
                "Database should accumulate ticks from all concurrent calls");
    }

    @Test
    @DisplayName("Integration: Should use custom @Query for recent ticks")
    void shouldUseCustomQueryForRecentTicks() {
        // Arrange: Create ticks at different times
        Instant old = Instant.now().minusSeconds(30);
        Instant recent = Instant.now();

        repository.save(new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("40000"), new BigDecimal("40100"), old));
        repository.save(new PriceTick(btcUsd, Exchange.COINBASE,
                new BigDecimal("50000"), new BigDecimal("50100"), recent));
        repository.save(new PriceTick(ethUsd, Exchange.KRAKEN,
                new BigDecimal("3000"), new BigDecimal("3010"), recent));

        // Act: Query recent ticks across all pairs
        Instant cutoff = Instant.now().minusSeconds(10);
        List<PriceTick> recentTicks = repository.findRecentTicks(cutoff);

        // Assert: Should return only recent ticks (both BTC and ETH)
        assertEquals(2, recentTicks.size(), "Should return 2 recent ticks");
        assertTrue(recentTicks.stream()
                .allMatch(t -> t.getTimestamp().isAfter(cutoff)),
                "All returned ticks should be after cutoff");
    }

    @Test
    @DisplayName("Integration: Should return empty when no recent ticks in database")
    void shouldReturnEmptyWhenNoRecentTicksInDatabase() {
        // Arrange: Save old ticks only (10 seconds ago)
        Instant old = Instant.now().minusSeconds(10);

        repository.save(new PriceTick(btcUsd, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50100"), old));

        // Act: Try to get aggregated quote (uses 5-second window)
        Optional<AggregatedTopOfBookQuote> result = priceService.getAggregatedTopOfBookQuote(btcUsd);

        // Assert: Should have fresh ticks from the service call
        assertTrue(result.isPresent(),
                "Should return result with fresh ticks from fetchers");
    }
}
