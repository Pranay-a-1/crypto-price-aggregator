package com.cryptoArb.repository;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import com.cryptoArb.domain_spring.ArbitrageOpportunity_spring;
import com.cryptoArb.domain_spring.CurrencyPair_spring;
import com.cryptoArb.domain_spring.Exchange_spring;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest focuses only on the JPA components,
 * scanning for @Entity classes and Spring Data repositories.
 */
@DataJpaTest
@ContextConfiguration(classes = CryptoPriceAggregatorApplication.class)
@EntityScan("com.cryptoArb.domain_spring")
@DisplayName("ArbitrageRepository JPA Slice Test")
class ArbitrageRepositoryTest {

    /**
     * TestEntityManager is a utility provided by Spring Boot
     * specifically for tests. It helps set up entity data
     * in a clean way, separate from the repository logic
     * we are trying to test.
     */
    @Autowired
    private TestEntityManager entityManager;

    /**
     * This line is the cause of our "Red" phase failure.
     * We are trying to inject a bean (ArbitrageRepository)
     * that does not exist.
     */
    @Autowired
    private ArbitrageRepository repository;

    @Test
    @DisplayName("Should save and retrieve an ArbitrageOpportunity")
    void shouldSaveAndRetrieveArbitrageOpportunity() {
        // --- Given ---
        // Create all the required embeddable parts
        CurrencyPair_spring pair = new CurrencyPair_spring("ETH", "USD");
        Exchange_spring buyExchange = new Exchange_spring("kraken");
        Exchange_spring sellExchange = new Exchange_spring("binance");
        Instant timestamp = Instant.now();

        // Create the entity we want to save
        ArbitrageOpportunity_spring opportunity = new ArbitrageOpportunity_spring(
                pair,
                timestamp,
                buyExchange,
                new BigDecimal("4000.00"),
                sellExchange,
                new BigDecimal("4001.00"),
                new BigDecimal("0.00025") // (4001 - 4000) / 4000
        );

        // --- When ---
        // We use the EntityManager to save the entity and get its ID
        ArbitrageOpportunity_spring saved = entityManager.persistAndFlush(opportunity);

        // We then use our (non-existent) repository to find it
        ArbitrageOpportunity_spring found = repository.findById(saved.getId()).orElse(null);

        // --- Then ---
        // We assert that the repository found the entity
        assertThat(found).isNotNull();

        // And that its data is correct
        assertThat(found.getPair().getBase()).isEqualTo("ETH");
        assertThat(found.getBuyExchange().getId()).isEqualTo("kraken");
        assertThat(found.getSellPrice()).isEqualByComparingTo("4001.00");
    }
}