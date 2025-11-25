package com.cryptoArb.repository;


import com.cryptoArb.domain_spring.PriceTick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for the PriceTick entity.
 *
 * By extending JpaRepository, we get a full set of CRUD methods
 * (save, findById, findAll, delete, etc.) for free.
 *<p>
 * The <PriceTick, Long> generics specify:
 * 1. PriceTick: The entity this repository manages.
 * 2. Long: The data type of the entity's primary key (@Id).
 * <p>
 * Added query method to fetch ticks by currency pair.
 */
@Repository // Good practice to annotate, though Spring can often infer it
public interface PriceTickRepository extends JpaRepository<PriceTick, Long> {

    /**
     * Finds all PriceTick records that match the given base and quote currency.
     * Spring Data JPA generates the SQL automatically based on this method name.
     *
     * @param base The base currency symbol (e.g., "BTC")
     * @param quote The quote currency symbol (e.g., "USD")
     * @return A list of matching PriceTick entities.
     *
     * JPA will find the method and generate the SQL automatically based on this method name.
     * to illustrate here it will generate the SQL query as:
     *
     * SELECT * FROM price_tick WHERE pair_base = ? AND pair_quote = ?
     *
     * The ? are placeholders for the parameters we pass to the method.
     */
    List<PriceTick> findByPairBaseAndPairQuote(String base, String quote);

    /**
     * Finds all PriceTick records for a currency pair after a specific timestamp.
     * Used for arbitrage detection to query recent price ticks.
     *
     * @param base      The base currency symbol (e.g., "BTC")
     * @param quote     The quote currency symbol (e.g., "USD")
     * @param timestamp The cutoff timestamp (only ticks after this time are
     *                  returned)
     * @return A list of matching PriceTick entities
     */
    List<PriceTick> findByPairBaseAndPairQuoteAndTimestampAfter(
            String base, String quote, Instant timestamp);
}