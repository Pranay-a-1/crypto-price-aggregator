package com.cryptoArb.crypto_price_aggregator.repository;

import com.cryptoArb.crypto_price_aggregator.domain.ArbitrageOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for ArbitrageOpportunity entities.
 * 
 * Extends JpaRepository for standard CRUD operations plus custom query methods.
 * Following Spring Data JPA conventions for query method naming.
 * 
 * TDD: Created to satisfy ArbitrageRepositoryTest requirements.
 */
@Repository
public interface ArbitrageRepository extends JpaRepository<ArbitrageOpportunity, Long> {

    /**
     * Find arbitrage opportunities for a specific currency pair,
     * ordered by detection time (most recent first).
     * 
     * Query method naming convention:
     * - findBy: SELECT query
     * - CurrencyPair_Base: Navigate to embedded CurrencyPair and filter by base
     * - And: Logical AND
     * - CurrencyPair_Quote: Navigate to embedded CurrencyPair and filter by quote
     * - OrderBy: Sort results
     * - DetectedAt: Field to sort by
     * - Desc: Descending order
     * 
     * @param base  Base currency (e.g., "BTC")
     * @param quote Quote currency (e.g., "USD")
     * @return List of opportunities for the currency pair, newest first
     */
    List<ArbitrageOpportunity> findByCurrencyPair_BaseAndCurrencyPair_QuoteOrderByDetectedAtDesc(
            String base,
            String quote);

    /**
     * Find arbitrage opportunities detected after a specific timestamp.
     * Useful for querying recent opportunities within a time window.
     * 
     * @param timestamp The cutoff time (opportunities after this will be returned)
     * @return List of opportunities detected after the timestamp
     */
    List<ArbitrageOpportunity> findByDetectedAtAfter(Instant timestamp);
}
