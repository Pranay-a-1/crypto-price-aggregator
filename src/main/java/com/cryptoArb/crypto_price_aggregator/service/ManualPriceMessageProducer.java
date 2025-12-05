package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.config.RabbitMqConfig;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Manual implementation of a RabbitMQ producer using raw Channel and Connection.
 * Represents the "Manual Implementation" phase of the roadmap.
 * <p>
 * This class listens for PriceTickFetchedEvent and manually publishes the tick to RabbitMQ.
 * It demonstrates the boilerplate required when not using RabbitTemplate.
 * <p>
 * <b>Note:</b> This bean is only active when the "manual" profile is active.
 * It is kept for educational purposes to demonstrate the low-level RabbitMQ API.
 */
@Service
@org.springframework.context.annotation.Profile("manual")
public class ManualPriceMessageProducer implements ApplicationListener<PriceTickFetchedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ManualPriceMessageProducer.class);

    private final CachingConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;

    public ManualPriceMessageProducer(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onApplicationEvent(PriceTickFetchedEvent event) {
        PriceTick tick = event.getPriceTick();
        log.info("Manual Producer: Publishing tick for {}/{} to RabbitMQ",
                tick.getPair().getBase(), tick.getPair().getQuote());

        Connection connection = null;
        Channel channel = null;
        try {
            // "Feel the pain": Managing connection and channel manually
            // Note: We use Spring's CachingConnectionFactory to get the underlying connection
            // because establishing a raw connection to a mock/embedded broker in tests is complex without it.
            // But we treat it as a raw connection.
            connection = connectionFactory.getRabbitConnectionFactory().newConnection();
            channel = connection.createChannel();

            String messageBody = objectMapper.writeValueAsString(tick);

            // Basic Publish
            String routingKey = "prices.tick." + tick.getExchange().name().toLowerCase();
            channel.basicPublish(
                    RabbitMqConfig.EXCHANGE_NAME,
                    routingKey,
                    null,
                    messageBody.getBytes()
            );

            log.debug("Manual Producer: Successfully published message: {}", messageBody);

        } catch (IOException | TimeoutException e) {
            log.error("Manual Producer: Failed to publish message", e);
            throw new RuntimeException("Failed to publish to RabbitMQ manually", e);
        } finally {
            // "Feel the pain": Manual cleanup
            try {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (IOException | TimeoutException e) {
                log.error("Manual Producer: Error closing resources", e);
            }
        }
    }
}
