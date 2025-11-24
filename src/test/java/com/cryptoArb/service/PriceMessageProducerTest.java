package com.cryptoArb.service;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.fetcher.PriceFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceMessageProducerTest {

    private PriceMessageProducer priceMessageProducer;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private PriceFetcher priceFetcher;

    private String exchangeName = "test-exchange";
    private String routingKey = "test-routing-key";

    @BeforeEach
    void setUp() {
        // We pass a list containing our mock fetcher
        priceMessageProducer = new PriceMessageProducer(List.of(priceFetcher), rabbitTemplate);

        // Manually set the @Value fields
        ReflectionTestUtils.setField(priceMessageProducer, "exchangeName", exchangeName);
        ReflectionTestUtils.setField(priceMessageProducer, "routingKey", routingKey);
    }

    @Test
    void shouldPublishPriceTickToRabbitMQ() throws com.cryptoArb.exception.PriceFetchException {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        com.cryptoArb.domain_spring.Exchange exchange = new com.cryptoArb.domain_spring.Exchange("mock-exchange");
        PriceTick tick = new PriceTick(pair, exchange, Instant.now(),
                new BigDecimal("50000"), new BigDecimal("50100"));

        when(priceFetcher.getExchangeName()).thenReturn("MockExchange");
        when(priceFetcher.fetchPrices()).thenReturn(List.of(tick));

        // When
        priceMessageProducer.fetchAndPublish();

        // Then
        verify(rabbitTemplate).convertAndSend(eq(exchangeName), eq(routingKey), eq(tick));
    }
}
