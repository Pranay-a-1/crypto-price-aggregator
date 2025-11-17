package com.cryptoArb.service;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;

import java.util.Optional;

/**
 * Spring-managed service interface for price-related operations.
 * This defines the contract for our application's services,
 * decoupling the controller from the implementation.
 */
public interface PriceService {

    /**
     * Retrieves the latest consolidated price for a specific currency pair.
     *
     * @param pair The CurrencyPair to find.
     * @return An Optional containing the ConsolidatedPrice if found, or empty if not.
     */
    Optional<ConsolidatedPrice> getConsolidatedPriceForPair(CurrencyPair pair);

    // We will add more methods as the API requirements grow.
}