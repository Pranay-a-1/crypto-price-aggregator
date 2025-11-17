package com.cryptoArb.service;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring-managed service interface for arbitrage-related operations.
 * This defines the contract for our application's services.
 *
 * (Our test currently mocks this bean, but it doesn't call any methods on it.
 * We will add methods here in a later TDD cycle when we test the
 * /api/v1/arbitrage endpoint.)
 */
@Service
public interface ArbitrageService {

    // No methods required by the test *yet*.
    // We will add List<ArbitrageOpportunity> getRecentOpportunities();
    // when we write the test for that endpoint.

    /**
     * Retrieves a list of recently found arbitrage opportunities.
     * @return A list of ArbitrageOpportunity objects.
     */
    List<ArbitrageOpportunity> getRecentOpportunities();
}