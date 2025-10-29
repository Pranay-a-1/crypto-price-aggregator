package com.cryptoArb.service;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceServiceTest {


    // Our mock data
    private Exchange coinbase = new Exchange("coinbase");
    private Exchange kraken = new Exchange("kraken");
    private CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");

    // 2. Define our timestamps as Instant objects
    private Instant ts1 = Instant.ofEpochMilli(1000L);
    private Instant ts2 = Instant.ofEpochMilli(1001L);
    private Instant ts3 = Instant.ofEpochMilli(1002L);

    private List<PriceTick> allTicks;

    @BeforeEach
    void setUp() {
        // Create a list of mixed ticks before each test
        allTicks = List.of(
                new PriceTick(btcUsd, coinbase, ts1, new BigDecimal("50000"), new BigDecimal("50001")),
                new PriceTick(btcUsd, kraken, ts2, new BigDecimal("49999"), new BigDecimal("50000")),
                new PriceTick(btcUsd, coinbase, ts3, new BigDecimal("50002"), new BigDecimal("50003"))
        );
    }

    @Test
    @DisplayName("Should filter a list of ticks and return only those from Coinbase")
    void givenTicksFromMultipleExchanges_whenFilterByCoinbase_thenReturnsOnlyCoinbaseTicks() {
        // Given: A PriceService and a list of ticks (from setUp)
        // These two lines will NOT compile
        PriceService priceService = new PriceService();
        String targetExchangeId = "coinbase";

        // When: We call the hard-coded filter method
        List<PriceTick> coinbaseTicks = priceService.filterCoinbaseTicks(allTicks);

        // Then: The resulting list should only have the 2 coinbase ticks
        assertEquals(2, coinbaseTicks.size(), "Should only be 2 Coinbase ticks");

        // And we can double-check that they are ALL from coinbase
        for (PriceTick tick : coinbaseTicks) {
            assertEquals(targetExchangeId, tick.exchange().id(), "Tick should be from Coinbase");
        }
    }




    @Test
    @DisplayName("Should filter ticks using a flexible Predicate (anonymous class)")
    void givenTicks_whenFilterByPredicate_thenReturnsFilteredTicks() {
        // Given: A PriceService (this time, it needs to be instantiated in the test)
        PriceService priceService = new PriceService();

        // And a Predicate, written as an anonymous class, to find "kraken" ticks
        //BEFORE:
        // Predicate<PriceTick> krakenPredicate = new Predicate<PriceTick>() {
        //     @Override
        //     public boolean test(PriceTick tick) {
        //         return "kraken".equals(tick.exchange().id());
        //     }
        // };

        // AFTER (Refactored to a Lambda):
        Predicate<PriceTick> krakenPredicate = tick -> "kraken".equals(tick.exchange().id());

        // When: We call our NEW, non-existent filter method
        // This line will NOT compile
        List<PriceTick> krakenTicks = priceService.filter(allTicks, krakenPredicate);

        // Then: The result should be correct
        assertEquals(1, krakenTicks.size(), "Should find the 1 kraken tick");
        assertEquals("kraken", krakenTicks.get(0).exchange().id());
    }





    @Test
    @DisplayName("Should filter by multiple criteria using Predicate.and()")
    void givenTicks_whenFilterByExchangeAndPrice_thenReturnsSpecificTick() {
        // Given: A PriceService
        PriceService priceService = new PriceService();

        // And: A simple Predicate for the exchange
        Predicate<PriceTick> coinbasePredicate =
                tick -> "coinbase".equals(tick.exchange().id());

        // And: A simple Predicate for the price
        // (Our mock data has coinbase ticks at 50001 and 50003 ask)
        Predicate<PriceTick> pricePredicate =
                tick -> tick.askPrice().compareTo(new BigDecimal("50002")) < 0;

        // When: We compose these predicates
        // This is the new behavior we are testing
        Predicate<PriceTick> combinedPredicate = coinbasePredicate.and(pricePredicate);

        // And: We use our *existing* filter method
        List<PriceTick> result = priceService.filter(allTicks, combinedPredicate);

        // Then: We should get only the 1 tick that matches *both*
        assertEquals(1, result.size(), "Should only find one matching tick");

        // We can be extra-specific and check the timestamp
        assertEquals(ts1, result.get(0).timestamp(), "Should be the first coinbase tick");
    }





    // We need a few more CurrencyPairs for this test
    private CurrencyPair ethUsd = new CurrencyPair("ETH", "USD");
    private CurrencyPair btcEur = new CurrencyPair("BTC", "EUR");

    @Test
    @DisplayName("Should sort ticks by pair, then by ascending ask price")
    void givenUnsortedTicks_whenSortByPairAndPrice_thenReturnsSortedList() {
        // Given: An unsorted list of ticks
        // Note: We use new ArrayList<>(List.of(...)) so it's mutable (sortable)
        List<PriceTick> unsortedTicks = new ArrayList<>(List.of(
                new PriceTick(ethUsd, coinbase, Instant.ofEpochMilli(1000L), new BigDecimal("3000"), new BigDecimal("3001")),  // ETH/USD
                new PriceTick(btcUsd, coinbase, Instant.ofEpochMilli(1001L), new BigDecimal("50002"), new BigDecimal("50003")), // BTC/USD (High Price)
                new PriceTick(btcEur, kraken, Instant.ofEpochMilli(1002L), new BigDecimal("45000"), new BigDecimal("45001")),  // BTC/EUR
                new PriceTick(btcUsd, kraken, Instant.ofEpochMilli(1003L), new BigDecimal("50000"), new BigDecimal("50001"))   // BTC/USD (Low Price)
        ));

        // When: We apply our sorting logic (this is the part we'll build)
        // ... (This is the "Red" part - we haven't sorted it yet!) ...

        // Then: The list should be sorted
        // 1. BTC/EUR (alphabetically "BTC/EUR" comes before "BTC/USD")
        // 2. BTC/USD (Low Price)
        // 3. BTC/USD (High Price)
        // 4. ETH/USD

//        // --- NEW CODE BELOW ---
//
//        // When: We define our composite sorting logic
//        // 1. First, sort by the pair's base currency (e.g., "BTC")
//        Comparator<PriceTick> comparator = Comparator
//                .comparing(tick -> tick.pair().base());
//
//        // 2. Next, sort by the pair's quote currency (e.g., "EUR" vs "USD")
//        comparator = comparator
//                .thenComparing(tick -> tick.pair().quote());
//
//
//        // 3. Finally, sort by the ask price (lowest to highest)
//        comparator = comparator
//                .thenComparing(PriceTick::askPrice);
//
//        // And we apply the sort to our list
//        unsortedTicks.sort(comparator);
//
//        // --- END OF NEW CODE ---

//      --- NEW "GREEN" CODE ---

        // BEFORE (Using Lambdas, which is also fine):
        // Comparator<PriceTick> byPair = Comparator.comparing(tick -> tick.pair());
        // Comparator<PriceTick> byAskPrice = Comparator.comparing(tick -> tick.askPrice());
        // When: We define our composite sorting logic

        // 1. Create a Comparator that extracts the CurrencyPair from a PriceTick
        // We use a "method reference" PriceTick::pair
        Comparator<PriceTick> byPair = Comparator.comparing(PriceTick::pair);

        // 2. Create a Comparator that extracts the askPrice
        Comparator<PriceTick> byAskPrice = Comparator.comparing(PriceTick::askPrice);

        // 3. Chain them together!
        Comparator<PriceTick> compositeComparator = byPair.thenComparing(byAskPrice);

        // And we apply the sort
        unsortedTicks.sort(compositeComparator);

        // --- END OF NEW CODE ---

        // Then: The list should be sorted
        // (These assertions will now PASS)
        assertEquals(btcEur, unsortedTicks.get(0).pair(), "First should be BTC/EUR");
        assertEquals(btcUsd, unsortedTicks.get(1).pair(), "Second should be BTC/USD (Low)");
        assertEquals(new BigDecimal("50001"), unsortedTicks.get(1).askPrice(), "Second should be BTC/USD (Low)");
        assertEquals(btcUsd, unsortedTicks.get(2).pair(), "Third should be BTC/USD (High)");
        assertEquals(ethUsd, unsortedTicks.get(3).pair(), "Fourth should be ETH/USD");
    }
}