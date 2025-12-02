package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.config.RabbitMqConfig;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PriceMessageProducer producer;

    @Test
    void shouldPublishEventUsingRabbitTemplate() {
        // Arrange
        PriceTick tick = new PriceTick(
                CurrencyPair.of("BTC", "USD"),
                Exchange.BINANCE,
                new BigDecimal("50000"),
                new BigDecimal("50100"),
                Instant.now()
        );
        PriceTickFetchedEvent event = new PriceTickFetchedEvent(this, tick);

        // Act
        producer.onApplicationEvent(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                "prices.tick.binance",
                tick
        );
    }
}
