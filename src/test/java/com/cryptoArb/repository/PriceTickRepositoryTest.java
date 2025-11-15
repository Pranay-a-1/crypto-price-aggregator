package com.cryptoArb.repository;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * This is our first "slice test" as planned for Phase 11.
 * It tests the persistence layer in isolation.
 *
 * It will fail to run for multiple reasons:
 * 1. PriceTickRepository (the interface) does not exist.
 * 2. PriceTick (the record) is not a JPA @Entity.
 * 3. PriceTick has no @Id field, which JPA requires.
 */
@DataJpaTest
@ContextConfiguration(classes = CryptoPriceAggregatorApplication.class)
class PriceTickRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceTickRepository repository;

    @Test
    void shouldSaveAndRetrievePriceTick() {
        // Given
        PriceTick tick = new PriceTick(
                new CurrencyPair("BTC", "USD"),
                new Exchange("coinbase"),
                Instant.now(),
                new BigDecimal("50000.00"),
                new BigDecimal("50001.00")
        );

        // When
        PriceTick saved = repository.save(tick);
        entityManager.flush();

        PriceTick found = repository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getPair().getBase()).isEqualTo("BTC");
        assertThat(found.getPair().getQuote()).isEqualTo("USD");
        assertThat(found.getExchange().getId()).isEqualTo("coinbase");
    }
}