package com.cryptoArb.service;

import com.cryptoArb.domain.ConsolidatedPrice;
import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.PriceTick;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PriceService {


    /**
     * Filters a list of ticks based on a dynamic condition. (Refactored to Streams)
     * @param allTicks The complete list of price ticks.
     * @param predicate The condition to apply.
     * @return A new list containing only matching ticks.
     */
    public List<PriceTick> filter(List<PriceTick> allTicks, Predicate<PriceTick> predicate) {
        return allTicks.stream()                // 1. Get a stream from the list
                .filter(predicate)              // 2. Apply the filter (the predicate)
                .collect(Collectors.toList());  // 3. Collect the results back into a new List
    }



    // --- NEW METHOD BELOW ---


    /**
     * REFACTORED: This method now delegates its logic
     * to the more flexible filter method.
     * Filters a list of ticks and returns only those from Coinbase.
     * This is a simple, hard-coded implementation for our TDD cycle.
     */
    public List<PriceTick> filterCoinbaseTicks(List<PriceTick> allTicks) {

        // This is the logic for "is this a coinbase tick?"
        // BEFORE:
        // Predicate<PriceTick> coinbasePredicate = new Predicate<PriceTick>() {
        //     @Override
        //     public boolean test(PriceTick tick) {
        //         return "coinbase".equals(tick.exchange().id());
        //     }
        // };

        // AFTER (Refactored to a Lambda):
        Predicate<PriceTick> coinbasePredicate = tick -> "coinbase".equals(tick.exchange().id());

        // This line is unchanged
        return filter(allTicks, coinbasePredicate);
    }

    // We can even simplify it further by not using a variable
    // and passing the lambda directly:
    public List<PriceTick> filterCoinbaseTicks_RefactoredEvenMore(List<PriceTick> allTicks) {
        return filter(allTicks, tick -> "coinbase".equals(tick.exchange().id()));
    }





    /**
     * Filters a list of ticks and returns only those from Coinbase.
     * This is a simple, hard-coded implementation for our TDD cycle.
     * @deprecated Hard-coded logic, replaced by {@link #filter(List, Predicate)}
     */
    @Deprecated
    public List<PriceTick> filterCoinbaseTicks_old(List<PriceTick> allTicks) {

        // 1. Create a new empty list to hold the results
        List<PriceTick> result = new ArrayList<>();

        // 2. Loop through all the ticks provided
        for (PriceTick tick : allTicks) {
            // 3. Apply the hard-coded filter logic
            // We use .equals() for safe string comparison
            if ("coinbase".equals(tick.exchange().id())) {
                // 4. If it matches, add it to our result list
                result.add(tick);
            }
        }

        // 5. Return the filtered list
        return result;
    }




    /**
     * Filters a list of ticks using a provided Predicate.
     * This is our new, flexible "behavior parameterized" method.
     * @param allTicks  The complete list of ticks to filter.
     * @param predicate The condition to test each tick against.
     * @return A new list containing only the ticks that passed the test.
     * @deprecated Replaced old hard-coded filtering methods.
     */
    @Deprecated
    public List<PriceTick> filter_old(List<PriceTick> allTicks, Predicate<PriceTick> predicate) {

        // 1. Create a new empty list
        List<PriceTick> result = new ArrayList<>();

        // 2. Loop through all the ticks
        for (PriceTick tick : allTicks) {
            // 3. Apply the predicate's logic
            // The .test() method returns true or false
            if (predicate.test(tick)) {
                // 4. If it's a match, add it to the list
                result.add(tick);
            }
        }

        // 5. Return the filtered list
        return result;
    }


    /**
     * Aggregates a list of raw price ticks into a map, where each currency pair
     * maps to its consolidated price (best bid, best ask, etc.).
     *
     * @param ticks A list of PriceTick objects from various exchanges.
     * @return A Map of CurrencyPair to its corresponding ConsolidatedPrice.
     */
    public Map<CurrencyPair, ConsolidatedPrice> aggregatePrices(List<PriceTick> ticks) {
        // We group all ticks by their currency pair.
        // The result is a Map<CurrencyPair, List<PriceTick>>
        // We then transform that map into our final Map<CurrencyPair, ConsolidatedPrice>
        return ticks.stream()
                .collect(Collectors.groupingBy(
                        PriceTick::pair, // Group by the CurrencyPair
                        // For each group (List<PriceTick>), we need to convert it into a ConsolidatedPrice
                        // collectingAndThen allows us to first collect into a List, then transform that List
                        Collectors.collectingAndThen( // As a downstream collector...
                                Collectors.toList(),     // ...first collect ticks into a List
                                this::buildConsolidatedPriceFromList // ...then pass that list to a helper ( old way : ticksList -> buildConsolidatedPriceFromList(ticksList)
                        )

                ));
    }


    /**
     * Aggregates a list of raw price ticks using a PARALLEL stream.
     * This is for our Phase 2 benchmark.
     *
     * @param ticks A list of PriceTick objects.
     * @return A Map of CurrencyPair to its corresponding ConsolidatedPrice.
     */
    public Map<CurrencyPair, ConsolidatedPrice> aggregatePricesParallel(List<PriceTick> ticks) {
        return ticks.parallelStream() // <-- PARALLEL stream
                .collect(Collectors.groupingBy(
                        PriceTick::pair,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::buildConsolidatedPriceFromList
                        )
                ));
    }


    /**
     * A helper method to convert a list of ticks (for a *single* currency pair)
     * into one ConsolidatedPrice object.
     *
     * @param ticksForPair A list of ticks, all for the same CurrencyPair.
     * @return A ConsolidatedPrice object.
     */
    private ConsolidatedPrice buildConsolidatedPriceFromList(List<PriceTick> ticksForPair) {
        // Find the tick with the HIGHEST bid price
        PriceTick bestBidTick = ticksForPair.stream()
                .max(Comparator.comparing(PriceTick::bidPrice))
                .orElseThrow(); // We assume the list is not empty based on groupingBy

        // Find the tick with the LOWEST ask price
        PriceTick bestAskTick = ticksForPair.stream()
                .min(Comparator.comparing(PriceTick::askPrice))
                .orElseThrow();

        // Find the tick with the LATEST timestamp
        PriceTick latestTick = ticksForPair.stream()
                .max(Comparator.comparing(PriceTick::timestamp)) // old way: (t1, t2) -> t1.timestamp().compareTo(t2.timestamp()) or Comparator.comparingLong(t -> t.timestamp().toEpochMilli())
                .orElseThrow();

        // Get the currency pair (they are all the same)
        CurrencyPair pair = ticksForPair.get(0).pair();

        // Create and return the new consolidated price
        return new ConsolidatedPrice(
                pair,
                latestTick.timestamp(),
                bestBidTick.bidPrice(),
                bestBidTick.exchange().id(), // Get the exchange from the best bid tick
                bestAskTick.askPrice(),
                bestAskTick.exchange().id()  // Get the exchange from the best ask tick
        );
    }





// --- TRADITIONAL AGGREGATION METHODS (For Reference) ---

    /**
     * TRADITIONAL, loop-based implementation of aggregatePrices.
     * This is for educational reference to understand what the Streams version does.
     *
     * @deprecated Use {@link #aggregatePrices(List)} for the modern implementation.
     */
    @Deprecated
    public Map<CurrencyPair, ConsolidatedPrice> aggregatePrices_traditional(List<PriceTick> ticks) {
        // 1. Create an intermediate map to group ticks by pair
        //    This is what `Collectors.groupingBy` does for us.
        Map<CurrencyPair, List<PriceTick>> mapOfTicksPerPair = new HashMap<>();

        // 2. Loop through every single tick
        for (PriceTick tick : ticks) {
            CurrencyPair pair = tick.pair();

            // 3. Check if we have seen this pair before
            if (!mapOfTicksPerPair.containsKey(pair)) {
                // If not, create a new empty list for it
                mapOfTicksPerPair.put(pair, new ArrayList<>());
            }

            // 4. Add the current tick to its pair's list
            mapOfTicksPerPair.get(pair).add(tick);
        }

        // 5. Now, create the final map we want to return
        Map<CurrencyPair, ConsolidatedPrice> finalConsolidatedMap = new HashMap<>();

        // 6. Loop through our intermediate map (one entry for each pair)
        //    This is what `Collectors.collectingAndThen` handles for us.
        // sample entry: BTC/USD -> [tick1, tick2, tick3...]
        // sample entrySet: Set<Map.Entry<CurrencyPair, List<PriceTick>>>
        for (Map.Entry<CurrencyPair, List<PriceTick>> entry : mapOfTicksPerPair.entrySet()) {
            CurrencyPair pair = entry.getKey();
            List<PriceTick> ticksForThisPair = entry.getValue();

            // 7. Call our traditional helper to find the best bid/ask for this pair
            ConsolidatedPrice consolidatedPrice = buildConsolidatedPriceFromList_traditional(ticksForThisPair);

            // 8. Add the final object to our result map
            finalConsolidatedMap.put(pair, consolidatedPrice);
        }

        // 9. Return the completed map
        return finalConsolidatedMap;
    }

    /**
     * TRADITIONAL, loop-based helper to find best bid/ask/latest time from a list.
     *
     * @deprecated Use the Stream-based helper {@link #buildConsolidatedPriceFromList(List)}
     */
    @Deprecated
    private ConsolidatedPrice buildConsolidatedPriceFromList_traditional(List<PriceTick> ticksForPair) {
        // We can't do anything if the list is empty (this shouldn't happen)
        if (ticksForPair == null || ticksForPair.isEmpty()) {
            return null;
        }

        // 1. Assume the first tick is the "best" to start
        //    This is what `max()` and `min()` do internally.
        PriceTick bestBidTick = ticksForPair.get(0);
        PriceTick bestAskTick = ticksForPair.get(0);
        PriceTick latestTick = ticksForPair.get(0);

        // 2. Loop through all the *other* ticks for this pair
        //    (We can skip the first tick, so we start i = 1)
        for (int i = 1; i < ticksForPair.size(); i++) {
            PriceTick currentTick = ticksForPair.get(i);

            // 3. Check for a new best bid (HIGHEST)
            //    (compareTo > 0 means currentTick.bidPrice is LARGER)
            if (currentTick.bidPrice().compareTo(bestBidTick.bidPrice()) > 0) {
                bestBidTick = currentTick;
            }

            // 4. Check for a new best ask (LOWEST)
            //    (compareTo < 0 means currentTick.askPrice is SMALLER)
            if (currentTick.askPrice().compareTo(bestAskTick.askPrice()) < 0) {
                bestAskTick = currentTick;
            }

            // 5. Check for a new latest timestamp
            if (currentTick.timestamp().isAfter(latestTick.timestamp())) {
                latestTick = currentTick;
            }
        }

        // 6. Get the pair (they are all the same, so we can use any tick)
        CurrencyPair pair = latestTick.pair();

        // 7. Create and return the new consolidated price
        return new ConsolidatedPrice(
                pair,
                latestTick.timestamp(),
                bestBidTick.bidPrice(),
                bestBidTick.exchange().id(),
                bestAskTick.askPrice(),
                bestAskTick.exchange().id()
        );
    }

}
