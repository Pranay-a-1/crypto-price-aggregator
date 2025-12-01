package com.cryptoArb.crypto_price_aggregator.repository;

import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA Repository for PriceTick entities.
 * 
 * Provides CRUD operations and custom queries for price data.
 * Spring Data JPA automatically implements these methods at runtime.
 * 
 * Following Repository Pattern:
 * - Abstracts data access layer from business logic
 * - Enables easy testing with mocks
 * - Leverages Spring Data's query derivation
 */
@Repository
public interface PriceTickRepository extends JpaRepository<PriceTick, Long> {

    /**
     * Find all ticks for a specific currency pair.
     * Query derived from method name by Spring Data JPA.
     * 
     * @param base  Base currency (e.g., "BTC")
     * @param quote Quote currency (e.g., "USD")
     * @return List of matching price ticks
     */
    List<PriceTick> findByPair_BaseAndPair_Quote(String base, String quote);

    /**
     * Find recent ticks for a specific currency pair after a given timestamp.
     * Used for aggregation (e.g., "last 5 seconds").
     * 
     * @param base      Base currency
     * @param quote     Quote currency
     * @param timestamp Cutoff time (exclusive)
     * @return List of recent price ticks
     */
    List<PriceTick> findByPair_BaseAndPair_QuoteAndTimestampAfter(
            String base, String quote, Instant timestamp);

    /**
     * Find all ticks from a specific exchange.
     * 
     * @param exchange The exchange to filter by
     * @return List of ticks from that exchange
     */
    List<PriceTick> findByExchange(Exchange exchange);

    /**
     * Custom JPQL query to find recent ticks across all pairs and exchanges.
     * 
     * @param since Cutoff timestamp
     * @return List of recent ticks, ordered by timestamp descending
     */
    @Query("SELECT pt FROM PriceTick pt WHERE pt.timestamp > :since ORDER BY pt.timestamp DESC")
    List<PriceTick> findRecentTicks(@Param("since") Instant since);
}
