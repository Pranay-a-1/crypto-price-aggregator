package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.repository.PriceTickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consumer that listens for PriceTickFetchedEvent and handles processing.
 * Initially, this just persists the tick to the database.
 */
@Component
public class PriceTickConsumer {

    private static final Logger log = LoggerFactory.getLogger(PriceTickConsumer.class);

    private final PriceTickRepository priceTickRepository;

    public PriceTickConsumer(PriceTickRepository priceTickRepository) {
        this.priceTickRepository = priceTickRepository;
    }

    @EventListener
    public void handlePriceTickFetchedEvent(PriceTickFetchedEvent event) {
        PriceTick tick = event.getPriceTick();
        log.debug("Received PriceTickFetchedEvent for {}", tick);

        priceTickRepository.save(tick);
        log.debug("Saved PriceTick to repository via event listener: {}", tick);
    }
}
