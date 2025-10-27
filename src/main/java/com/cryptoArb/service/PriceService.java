package com.cryptoArb.service;

import com.cryptoArb.domain.PriceTick;

import java.util.ArrayList;
import java.util.List;

public class PriceService {


    /**
     * Filters a list of ticks and returns only those from Coinbase.
     * This is a simple, hard-coded implementation for our TDD cycle.
     */
    public List<PriceTick> filterCoinbaseTicks(List<PriceTick> allTicks) {

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
}
