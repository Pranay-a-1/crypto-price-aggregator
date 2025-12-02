package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceTickConsumerTest {

    @Mock
    private PriceTickRepository priceTickRepository;

    private PriceTickConsumer priceTickConsumer;

    @BeforeEach
    void setUp() {
        priceTickConsumer = new PriceTickConsumer(priceTickRepository);
    }

    @Test
    void shouldSavePriceTickWhenEventIsReceived() {
        // Arrange
        PriceTick tick = new PriceTick(
                new CurrencyPair("BTC", "USD"),
                Exchange.BINANCE,
                new BigDecimal("50000.00"),
                new BigDecimal("50010.00"),
                Instant.now()
        );
        PriceTickFetchedEvent event = new PriceTickFetchedEvent(this, tick);

        // Act
        priceTickConsumer.handlePriceTickFetchedEvent(event);

        // Assert
        verify(priceTickRepository).save(tick);
    }
}
