package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.config.RabbitMqConfig;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer that listens for PriceTick messages from RabbitMQ and handles
 * processing.
 * <p>
 * Initially, this just persists the tick to the database.
 * <p>
 * <b>Phase 6 Update:</b> Migrated from @EventListener (Spring internal)
 * to @RabbitListener (External MQ).
 * <p>
 * <b>Phase 9 Update:</b> Added @Transactional to ensure atomicity with
 * PostgreSQL.
 *
 */
@Component
@Transactional
public class PriceTickConsumer {

    private static final Logger log = LoggerFactory.getLogger(PriceTickConsumer.class);

    private final PriceTickRepository priceTickRepository;

    public PriceTickConsumer(PriceTickRepository priceTickRepository) {
        this.priceTickRepository = priceTickRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void handlePriceTickMessage(PriceTick tick) {
        log.debug("Received PriceTick from RabbitMQ: {}", tick);

        priceTickRepository.save(tick);
        log.debug("Saved PriceTick to repository via RabbitMQ listener: {}", tick);
    }
}
