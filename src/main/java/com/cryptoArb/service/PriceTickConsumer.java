package com.cryptoArb.service;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.repository.PriceTickRepository;
import com.cryptoArb.service.ArbitrageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Phase 14: Consumer Service.
 * Listens for PriceTick messages from RabbitMQ and processes them.
 * 
 * This service:
 * 1. Receives PriceTick messages from the queue
 * 2. Saves them to the database
 * 3. Triggers arbitrage detection logic
 */
@Service
public class PriceTickConsumer {

    private static final Logger log = LoggerFactory.getLogger(PriceTickConsumer.class);

    private final PriceTickRepository priceTickRepository;
    private final ArbitrageService arbitrageService;

    @Autowired
    public PriceTickConsumer(PriceTickRepository priceTickRepository,
            ArbitrageService arbitrageService) {
        this.priceTickRepository = priceTickRepository;
        this.arbitrageService = arbitrageService;
    }

    /**
     * Processes incoming PriceTick messages from RabbitMQ.
     * 
     * The @RabbitListener annotation automatically:
     * - Deserializes the JSON message to a PriceTick object
     * - Handles connection management
     * - Provides retry logic on failure
     * 
     * @param tick The PriceTick message received from the queue
     */
    @RabbitListener(queues = "${cpa.rabbitmq.queue}")
    public void processPriceTick(PriceTick tick) {
        log.info("Received PriceTick from RabbitMQ: exchange={}, pair={}/{}, bid={}, ask={}",
                tick.getExchange().getId(),
                tick.getPair().getBase(),
                tick.getPair().getQuote(),
                tick.getBidPrice(),
                tick.getAskPrice());

        try {
            // Step 1: Save the PriceTick to the database
            PriceTick savedTick = priceTickRepository.save(tick);
            log.debug("Saved PriceTick to database with ID: {}", savedTick.getId());

            // Step 2: Trigger arbitrage detection for this currency pair
            arbitrageService.detectAndSaveOpportunities(tick.getPair());

            log.info("Successfully processed PriceTick for {}/{}",
                    tick.getPair().getBase(), tick.getPair().getQuote());

        } catch (Exception e) {
            log.error("Failed to process PriceTick: exchange={}, pair={}/{}, error={}",
                    tick.getExchange().getId(),
                    tick.getPair().getBase(),
                    tick.getPair().getQuote(),
                    e.getMessage(), e);
            // Re-throw to trigger RabbitMQ retry mechanism
            throw new RuntimeException("Failed to process PriceTick", e);
        }
    }
}
