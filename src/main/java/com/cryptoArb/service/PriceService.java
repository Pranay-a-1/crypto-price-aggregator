package com.cryptoArb.service;

import com.cryptoArb.domain.PriceTick;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PriceService {


    /**
     * Filters a list of ticks and returns only those from Coinbase.
     * This is a simple, hard-coded implementation for our TDD cycle.
     */
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









    // --- NEW METHOD BELOW ---


    /**
     * REFACTORED: This method now delegates its logic
     * to the more flexible filter method.
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
     * Filters a list of ticks using a provided Predicate.
     * This is our new, flexible "behavior parameterized" method.
     */
    public List<PriceTick> filter(List<PriceTick> allTicks, Predicate<PriceTick> predicate) {

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



}
