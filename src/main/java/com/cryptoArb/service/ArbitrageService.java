package com.cryptoArb.service;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;

import java.time.Duration;
import java.util.List;

/**
 * Spring-managed service interface for arbitrage-related operations.
 * This defines the contract for our application's services.
 *
 * (Our test currently mocks this bean, but it doesn't call any methods on it.
 * We will add methods here in a later TDD cycle when we test the
 * /api/v1/arbitrage endpoint.)
 */
public interface ArbitrageService {

    /**
     * Retrieves a list of arbitrage opportunities found in the last 5 minutes (default).
     * @return A list of recent ArbitrageOpportunity objects.
     */
    List<ArbitrageOpportunity> getRecentOpportunities();

    /**
     * Retrieves a list of arbitrage opportunities found within the specified duration.
     *
     * @param duration The look back period (e.g., Duration.ofHours(1)).
     * @return A list of ArbitrageOpportunity objects.
     */
    List<ArbitrageOpportunity> getRecentOpportunities(Duration duration);
}