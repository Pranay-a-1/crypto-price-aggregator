package com.cryptoArb.service;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Phase 14: Producer Engine.
 * Fetches prices from all registered fetchers and publishes them to RabbitMQ.
 * This decouples fetching from processing.
 */
@Service
public class PriceMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(PriceMessageProducer.class);

    private final List<PriceFetcher> fetchers;
    private final RabbitTemplate rabbitTemplate;

    @Value("${cpa.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${cpa.rabbitmq.routing-key}")
    private String routingKey;

    @Autowired
    public PriceMessageProducer(List<PriceFetcher> fetchers, RabbitTemplate rabbitTemplate) {
        this.fetchers = fetchers;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Fetches prices from all fetchers and publishes them to the message queue.
     */
    public void fetchAndPublish() {
        log.info("Starting async fetch-and-publish cycle...");

        for (PriceFetcher fetcher : fetchers) {
            try {
                log.debug("Fetching from {}", fetcher.getExchangeName());
                List<PriceTick> ticks = fetcher.fetchPrices();

                for (PriceTick tick : ticks) {
                    log.debug("Publishing tick to RabbitMQ: {}", tick);
                    rabbitTemplate.convertAndSend(exchangeName, routingKey, tick);
                }
            } catch (PriceFetchException e) {
                log.error("Failed to fetch prices from {}: {}", fetcher.getExchangeName(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error during fetch/publish for {}: {}", fetcher.getExchangeName(), e.getMessage(),
                        e);
            }
        }
        log.info("Async fetch-and-publish cycle completed.");
    }
}
