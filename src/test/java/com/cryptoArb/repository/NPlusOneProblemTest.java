package com.cryptoArb.repository;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.ExchangeInfo;
import com.cryptoArb.domain_spring.LinkedArbitrageOpportunity;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the solution to the N+1 Select Problem.
 */
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class NPlusOneProblemTest {

    @Autowired
    private LinkedArbitrageRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // 1. Create Reference Data
        ExchangeInfo coinbase = new ExchangeInfo("coinbase", "Coinbase Pro", "https://coinbase.com", 0.005);
        ExchangeInfo kraken = new ExchangeInfo("kraken", "Kraken Exchange", "https://kraken.com", 0.0026);
        ExchangeInfo binance = new ExchangeInfo("binance", "Binance", "https://binance.com", 0.001);

        entityManager.persist(coinbase);
        entityManager.persist(kraken);
        entityManager.persist(binance);

        // 2. Create Opportunities linked to these exchanges
        createOpportunity(coinbase, kraken);
        createOpportunity(coinbase, binance);
        createOpportunity(kraken, binance);
        createOpportunity(binance, coinbase);

        entityManager.flush();
        entityManager.clear(); // Clear L1 cache to force DB hits
    }

    private void createOpportunity(ExchangeInfo buy, ExchangeInfo sell) {
        LinkedArbitrageOpportunity opp = new LinkedArbitrageOpportunity(
                new CurrencyPair("BTC", "USD"),
                Instant.now(),
                buy, new BigDecimal("50000"),
                sell, new BigDecimal("50100"),
                new BigDecimal("0.002")
        );
        entityManager.persist(opp);
    }

    @Test
    @DisplayName("Verify N+1 Fix: findAllWithExchanges() should execute exactly 1 query")
    void testNPlusOneFixed() {
        // 1. Reset Statistics
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.clear();

        System.out.println("--- Executing Optimized Query (@EntityGraph) ---");

        // 2. Call the NEW, optimized method
        List<LinkedArbitrageOpportunity> opportunities = repository.findAllWithExchanges();

        System.out.println("--- Accessing Lazy Properties ---");
        // 3. Iterate and access the properties
        // Since we used @EntityGraph, these should already be loaded in memory.
        for (LinkedArbitrageOpportunity opp : opportunities) {
            String url = opp.getBuyExchange().getUrl();
            String sellUrl = opp.getSellExchange().getUrl();
        }

        // 4. Verify Query Count
        long queryCount = stats.getQueryExecutionCount();
        System.out.println("Total Queries Executed: " + queryCount);

        // Assertion:
        // It should be exactly 1 query (The big JOIN query).
        assertThat(queryCount).as("Should execute exactly 1 query").isEqualTo(1);
    }
}