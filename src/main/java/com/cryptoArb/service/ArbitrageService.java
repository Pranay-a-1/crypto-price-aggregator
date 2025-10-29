package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;
import com.cryptoArb.domain.ConsolidatedPrice;
import com.cryptoArb.domain.CurrencyPair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArbitrageService {
    /**
     * Finds arbitrage opportunities from a map of consolidated prices.
     * An opportunity exists if a pair's bestBid > bestAsk.
     *
     * @param priceMap A Map of CurrencyPair to ConsolidatedPrice.
     * @return A List of ArbitrageOpportunity objects.
     */
    public List<ArbitrageOpportunity> findArbitrageOpportunities(
            Map<CurrencyPair, ConsolidatedPrice> priceMap) {

        // We stream the *values* of the map (the ConsolidatedPrice objects)
        return priceMap.values().stream()
                //
                // 1. Red Phase: Filter (Find the opportunities)
                //    Keep only prices where we can sell high (bestBid)
                //    and buy low (bestAsk).
                //
                .filter(price -> price.bestBid().compareTo(price.bestAsk()) > 0)
                //
                // 2. Green Phase: Map (Transform the data)
                //    Convert the ConsolidatedPrice object into an
                //    ArbitrageOpportunity object.
                //
                .map(this::createOpportunityFromPrice)
                //
                // 3. Refactor Phase: Collect (Return the result)
                //    Collect all found opportunities into a List.
                //
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a ConsolidatedPrice to an ArbitrageOpportunity.
     * Note the logic flip:
     * - We BUY at the Best ASK.
     * - We SELL at the Best BID.
     */
    private ArbitrageOpportunity createOpportunityFromPrice(ConsolidatedPrice price) {
        return new ArbitrageOpportunity(
                price.pair(),
                price.timestamp(),
                price.bestAskExchange(), // Buy from the exchange with the lowest ask
                price.bestAsk(),         // Buy at the lowest ask price
                price.bestBidExchange(), // Sell to the exchange with the highest bid
                price.bestBid()          // Sell at the highest bid price
        );
    }
}
