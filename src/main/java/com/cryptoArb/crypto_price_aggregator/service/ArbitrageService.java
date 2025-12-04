package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.ArbitrageOpportunity;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;

import java.util.List;
import java.util.Optional;

/**
 * Service for detecting and managing arbitrage opportunities.
 * 
 * Arbitrage: Buying an asset on one exchange at a lower price and
 * simultaneously selling it on another exchange at a higher price.
 * 
 * Following SOLID:
 * - Single Responsibility: Only handles arbitrage detection and retrieval
 * - Interface Segregation: Focused methods for specific use cases
 */
public interface ArbitrageService {

    /**
     * Detect arbitrage opportunity for a given currency pair by analyzing
     * the latest price ticks from all exchanges.
     * 
     * Algorithm:
     * 1. Fetch latest ticks for the pair from all exchanges
     * 2. Find the minimum ask price (where to buy)
     * 3. Find the maximum bid price (where to sell)
     * 4. If max bid > min ask, arbitrage exists
     * 5. Calculate profit percentage and persist
     * 
     * @param pair The currency pair to analyze
     * @return Optional containing the arbitrage opportunity if found, empty
     *         otherwise
     */
    Optional<ArbitrageOpportunity> detectArbitrage(CurrencyPair pair);

    /**
     * Get recent arbitrage opportunities for a specific currency pair.
     * Results are ordered by detection time (most recent first).
     * 
     * @param pair  The currency pair to query
     * @param limit Maximum number of opportunities to return
     * @return List of recent arbitrage opportunities
     */
    List<ArbitrageOpportunity> getRecentOpportunities(CurrencyPair pair, int limit);
}
