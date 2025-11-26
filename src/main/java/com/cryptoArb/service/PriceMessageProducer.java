package com.cryptoArb.service;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.fetcher.PriceFetcher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Phase 14: Producer Engine with Message Tracing.
 * Fetches prices from all registered fetchers and publishes them to RabbitMQ.
 * Now includes Micrometer metrics for observability.
 */
@Service
public class PriceMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(PriceMessageProducer.class);

    private final List<PriceFetcher> fetchers;
    private final RabbitTemplate rabbitTemplate;
    private final Counter messagesPublishedCounter;
    private final Counter publishFailureCounter;
    private final Timer publishDurationTimer;

    @Value("${cpa.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${cpa.rabbitmq.routing-key}")
    private String routingKey;

    @Autowired
    public PriceMessageProducer(List<PriceFetcher> fetchers, RabbitTemplate rabbitTemplate,
            MeterRegistry meterRegistry) {
        this.fetchers = fetchers;
        this.rabbitTemplate = rabbitTemplate;

        // Initialize custom metrics
        this.messagesPublishedCounter = Counter.builder("rabbitmq.messages.published")
                .description("Total number of messages published to RabbitMQ")
                .register(meterRegistry);

        this.publishFailureCounter = Counter.builder("rabbitmq.messages.publish.failed")
                .description("Total number of failed message publish attempts")
                .register(meterRegistry);

        this.publishDurationTimer = Timer.builder("rabbitmq.messages.publish.duration")
                .description("Time taken to publish messages to RabbitMQ")
                .register(meterRegistry);
    }

    /**
     * Fetches prices from all fetchers and publishes them to the message queue.
     * Now includes metrics tracking for each publish operation.
     */
    public void fetchAndPublish() {
        log.info("Starting async fetch-and-publish cycle...");

        for (PriceFetcher fetcher : fetchers) {
            try {
                log.debug("Fetching from {}", fetcher.getExchangeName());
                List<PriceTick> ticks = fetcher.fetchPrices();

                for (PriceTick tick : ticks) {
                    // Measure publish duration and track success/failure
                    publishDurationTimer.record(() -> {
                        try {
                            log.debug("Publishing tick to RabbitMQ: {}", tick);
                            rabbitTemplate.convertAndSend(exchangeName, routingKey, tick);
                            messagesPublishedCounter.increment();
                        } catch (Exception e) {
                            publishFailureCounter.increment();
                            throw e;
                        }
                    });
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
