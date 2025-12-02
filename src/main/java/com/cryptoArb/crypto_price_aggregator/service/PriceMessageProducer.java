package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.config.RabbitMqConfig;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Professional implementation of RabbitMQ producer using Spring's RabbitTemplate.
 * This replaces the manual implementation.
 */
@Service
@Primary // Ensure this one is injected if there's ambiguity, though strictly they are just listeners.
public class PriceMessageProducer implements ApplicationListener<PriceTickFetchedEvent> {

    private static final Logger log = LoggerFactory.getLogger(PriceMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public PriceMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void onApplicationEvent(PriceTickFetchedEvent event) {
        PriceTick tick = event.getPriceTick();
        log.debug("Professional Producer: Publishing tick for {}/{} to RabbitMQ",
                tick.getPair().getBase(), tick.getPair().getQuote());

        // Use RabbitTemplate to convert and send
        // We use a specific routing key for the exchange (e.g. prices.tick.binance)
        String routingKey = "prices.tick." + tick.getExchange().name().toLowerCase();

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                routingKey,
                tick
        );

        log.debug("Professional Producer: Published tick to exchange {}", RabbitMqConfig.EXCHANGE_NAME);
    }
}
