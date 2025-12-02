package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.config.RabbitMqConfig;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManualPriceMessageProducerTest {

    @Mock
    private CachingConnectionFactory cachingConnectionFactory;
    @Mock
    private ConnectionFactory rabbitConnectionFactory;
    @Mock
    private Connection connection;
    @Mock
    private Channel channel;

    private ManualPriceMessageProducer producer;

    @BeforeEach
    void setUp() throws Exception {
        when(cachingConnectionFactory.getRabbitConnectionFactory()).thenReturn(rabbitConnectionFactory);
        when(rabbitConnectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);

        producer = new ManualPriceMessageProducer(cachingConnectionFactory);
    }

    @Test
    void shouldPublishEventToRabbitMq() throws Exception {
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
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(channel).basicPublish(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                any(), // properties
                bodyCaptor.capture()
        );

        // Verify exchange and routing key
        // Note: Manual producer uses a hardcoded routing key in this implementation
        assertTrue(exchangeCaptor.getValue().equals(RabbitMqConfig.EXCHANGE_NAME));
        assertTrue(routingKeyCaptor.getValue().equals("prices.tick.binance"));

        // Verify payload contains price
        String json = new String(bodyCaptor.getValue());
        assertTrue(json.contains("50000"));
        assertTrue(json.contains("BTC"));

        // Verify resources closed
        // Note: verify(channel).close() might not happen if isOpen() returns false by default.
        // We avoid UnnecessaryStubbingException by not stubbing if we don't care,
        // or using lenient() if we want to be safe but it's not always called.
        // In the original run, the code checked isOpen() which returned false (default), so close() wasn't called.
        // We verified close(), which failed.
        // To verify close(), we MUST ensure isOpen() returns true.
    }

    @Test
    void shouldCloseResourcesInFinallyBlock() throws Exception {
        // Arrange
        PriceTick tick = new PriceTick(
                CurrencyPair.of("BTC", "USD"),
                Exchange.BINANCE,
                new BigDecimal("50000"),
                new BigDecimal("50100"),
                Instant.now()
        );
        PriceTickFetchedEvent event = new PriceTickFetchedEvent(this, tick);

        // Mock isOpen to true so cleanup triggers
        when(channel.isOpen()).thenReturn(true);
        when(connection.isOpen()).thenReturn(true);

        // Act
        producer.onApplicationEvent(event);

        // Assert
        verify(channel).close();
        verify(connection).close();
    }
}
