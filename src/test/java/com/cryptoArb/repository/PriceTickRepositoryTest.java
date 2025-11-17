package com.cryptoArb.repository;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
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