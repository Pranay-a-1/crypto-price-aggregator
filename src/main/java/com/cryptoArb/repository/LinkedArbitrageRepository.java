package com.cryptoArb.repository;

import com.cryptoArb.domain_spring.LinkedArbitrageOpportunity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the "Heavy" LinkedArbitrageOpportunity entity.
 * <p>
 * Refactored to include methods that solve the N+1 Select Problem.
 */
@Repository
public interface LinkedArbitrageRepository extends JpaRepository<LinkedArbitrageOpportunity, Long> {

    /**
     * Solution 1: The Declarative Fix (@EntityGraph).
     * <p>
     * This annotation tells Spring Data JPA to create a dynamic FetchGraph.
     * It overrides the entity's FetchType.LAZY setting for the specified attributes,
     * causing them to be fetched eagerly in a SINGLE query using a LEFT OUTER JOIN.
     */
    @EntityGraph(attributePaths = {"buyExchange", "sellExchange"})
    @Query("SELECT l FROM LinkedArbitrageOpportunity l")
    List<LinkedArbitrageOpportunity> findAllWithExchanges();

    /**
     * Solution 2: The Explicit Fix (JPQL JOIN FETCH).
     * <p>
     * This uses standard JPQL to force the fetch.
     * "JOIN FETCH" instructs the persistence provider to fetch the related
     * entities along with the parent in the same SELECT statement.
     */
    @Query("SELECT l FROM LinkedArbitrageOpportunity l LEFT JOIN FETCH l.buyExchange LEFT JOIN FETCH l.sellExchange")
    List<LinkedArbitrageOpportunity> findAllWithExchangesJpql();
}