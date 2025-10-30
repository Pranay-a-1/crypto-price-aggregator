package com.cryptoArb.service;

import com.cryptoArb.domain.ConsolidatedPrice;
import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceServiceTest {


    // Our mock data
    private Exchange coinbase = new Exchange("coinbase");
    private Exchange kraken = new Exchange("kraken");
    private CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
    private CurrencyPair ethUsd = new CurrencyPair("ETH", "USD");

    // 2. Define our timestamps as Instant objects
    private Instant ts1 = Instant.ofEpochMilli(1000L);
    private Instant ts2 = Instant.ofEpochMilli(1001L);
    private Instant ts3 = Instant.ofEpochMilli(1002L);
    private Instant ts4 = Instant.ofEpochMilli(1003L);
    private Instant ts5 = Instant.ofEpochMilli(1004L);
    private Instant ts6 = Instant.ofEpochMilli(1005L);

    private List<PriceTick> allTicks;
    private PriceService priceService;


    @BeforeEach
    void setUp() {
        priceService = new PriceService();
        // Create a list of mixed ticks before each test
        allTicks = List.of(
                // --- BTC/USD Ticks ---
                // Tick 1 (ts1): Coinbase, Bid: 50000, Ask: 50001
                new PriceTick(btcUsd, coinbase, ts1, new BigDecimal("50000"), new BigDecimal("50001")),
                // Tick 2 (ts2): Kraken, Bid: 50002 (Best Bid), Ask: 50003
                new PriceTick(btcUsd, kraken, ts2, new BigDecimal("50002"), new BigDecimal("50003")),
                // Tick 3 (ts3): Coinbase, Bid: 50001, Ask: 50000 (Best Ask)
                new PriceTick(btcUsd, coinbase, ts3, new BigDecimal("50001"), new BigDecimal("50000")),

                // --- ETH/USD Ticks ---
                // Tick 4 (ts4): Kraken, Bid: 3000 (Best Bid), Ask: 3002
                new PriceTick(ethUsd, kraken, ts4, new BigDecimal("3000"), new BigDecimal("3002")),
                // Tick 5 (ts5): Coinbase, Bid: 2999, Ask: 3001 (Best Ask)
                new PriceTick(ethUsd, coinbase, ts5, new BigDecimal("2999"), new BigDecimal("3001")),
                // Tick 6 (ts6): Coinbase, Bid: 2998, Ask: 3003
                new PriceTick(ethUsd, coinbase, ts6, new BigDecimal("2998"), new BigDecimal("3003"))
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
        // FIX: We now have 4 coinbase ticks in our new list
        assertEquals(4, coinbaseTicks.size(), "Should now be 4 Coinbase ticks");

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

        // Then:
        // FIX: We now have 2 kraken ticks in our new list
        assertEquals(2, krakenTicks.size(), "Should find the 2 kraken ticks");
        assertEquals("kraken", krakenTicks.get(0).exchange().id());
        assertEquals("kraken", krakenTicks.get(1).exchange().id());
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

//        System.out.println("Filtered Ticks: ");
//        for (PriceTick tick : result) {
//            System.out.println(" - " + tick.pair() + " | " + tick.exchange().id() + " | Ask: " + tick.askPrice());
//        }

        // Then:
        // FIX: The predicate correctly matches 4 ticks (ts1, ts3, ts5, ts6)
        assertEquals(4, result.size(), "Should find four matching ticks");

        // Add more robust checks to ensure we have the *right* 3 ticks
        assertTrue(result.stream().anyMatch(t -> t.timestamp().equals(ts1)), "Missing ts1 tick");
        assertTrue(result.stream().anyMatch(t -> t.timestamp().equals(ts3)), "Missing ts3 tick");
        assertTrue(result.stream().anyMatch(t -> t.timestamp().equals(ts5)), "Missing ts5 tick");
        assertTrue(result.stream().anyMatch(t -> t.timestamp().equals(ts6)), "Missing ts6 tick");
    }





    // We need a few more CurrencyPairs for this test
    private final CurrencyPair btcEur = new CurrencyPair("BTC", "EUR");

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

    @Test
    @DisplayName("Should aggregate a list of ticks into consolidated prices per pair")
    void givenTicks_whenAggregatePrices_thenReturnsMapOfConsolidatedPrices() {
        // (allTicks is provided by setUp)

        // When
        // This line will NOT COMPILE (RED)
        Map<CurrencyPair, ConsolidatedPrice> consolidatedMap = priceService.aggregatePrices(allTicks);

        // Then
        // We expect two entries in our map, one for BTC/USD and one for ETH/USD
        assertEquals(2, consolidatedMap.size(), "Map should contain 2 currency pairs");

        // --- Verify BTC/USD ---
        ConsolidatedPrice btcPrice = consolidatedMap.get(btcUsd);
        assertEquals(btcUsd, btcPrice.pair());
        assertEquals(new BigDecimal("50002"), btcPrice.bestBid(), "BTC Best Bid is incorrect");
        assertEquals("kraken", btcPrice.bestBidExchange(), "BTC Best Bid Exchange is incorrect");
        assertEquals(new BigDecimal("50000"), btcPrice.bestAsk(), "BTC Best Ask is incorrect");
        assertEquals("coinbase", btcPrice.bestAskExchange(), "BTC Best Ask Exchange is incorrect");
        assertEquals(ts3, btcPrice.timestamp(), "Timestamp should be the *latest* for that pair"); // ts3 is latest for BTC

        // --- Verify ETH/USD ---
        ConsolidatedPrice ethPrice = consolidatedMap.get(ethUsd);
        assertEquals(ethUsd, ethPrice.pair());
        assertEquals(new BigDecimal("3000"), ethPrice.bestBid(), "ETH Best Bid is incorrect");
        assertEquals("kraken", ethPrice.bestBidExchange(), "ETH Best Bid Exchange is incorrect");
        assertEquals(new BigDecimal("3001"), ethPrice.bestAsk(), "ETH Best Ask is incorrect");
        assertEquals("coinbase", ethPrice.bestAskExchange(), "ETH Best Ask Exchange is incorrect");
        assertEquals(ts6, ethPrice.timestamp(), "Timestamp should be the *latest* for that pair"); // ts6 is latest for ETH
    }




    @Test
    @DisplayName("Should return Optional of consolidated price for an existing pair")
    void shouldReturnOptionalOfPriceForExistingPair() {
        // GIVEN: A known currency pair from our 'allTicks' data
        CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");

        // WHEN: We get the consolidated price for that pair, passing in the ticks
        Optional<ConsolidatedPrice> result = priceService.getConsolidatedPriceForPair(allTicks, btcUsd);

        // THEN: The Optional should be present and contain the correct data
        assertTrue(result.isPresent(), "Optional should not be empty");
        assertEquals(btcUsd, result.get().pair(), "Currency pair should match");
        // Values below are from the setUp data
        assertEquals(new BigDecimal("50000"), result.get().bestAsk(), "Best ask should be correct"); // from ts3
        assertEquals(new BigDecimal("50002"), result.get().bestBid(), "Best bid should be correct"); // from ts2
    }

    @Test
    @DisplayName("Should return empty Optional for a non-existent pair")
    void shouldReturnEmptyOptionalForNonExistentPair() {
        // GIVEN: A currency pair that is not in our data
        CurrencyPair ethJpy = new CurrencyPair("ETH", "JPY");


        // WHEN: We get the consolidated price for that pair, passing in the ticks
        Optional<ConsolidatedPrice> result = priceService.getConsolidatedPriceForPair(allTicks, ethJpy);

        // THEN: The Optional should be empty
        assertTrue(result.isEmpty(), "Optional should be empty");
    }

}