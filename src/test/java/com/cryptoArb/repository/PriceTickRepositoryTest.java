package com.cryptoArb.repository;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import com.cryptoArb.domain_spring.CurrencyPair_spring;
import com.cryptoArb.domain_spring.Exchange_spring;
import com.cryptoArb.domain_spring.PriceTick_spring;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = CryptoPriceAggregatorApplication.class)
@EntityScan("com.cryptoArb.domain_spring")
class PriceTickRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceTickRepository repository;

    @Test
    void shouldSaveAndRetrievePriceTick() {
        // Given
        PriceTick_spring tick = new PriceTick_spring(
                new CurrencyPair_spring("BTC", "USD"),
                new Exchange_spring("coinbase"),
                Instant.now(),
                new BigDecimal("50000.00"),
                new BigDecimal("50001.00")
        );

        // When
        PriceTick_spring saved = repository.save(tick);
        entityManager.flush();

        PriceTick_spring found = repository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getPair().getBase()).isEqualTo("BTC");
        assertThat(found.getPair().getQuote()).isEqualTo("USD");
        assertThat(found.getExchange().getId()).isEqualTo("coinbase");
    }
}