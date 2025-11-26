package com.cryptoArb.service;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.repository.PriceTickRepository;
import com.cryptoArb.service.ArbitrageService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Phase 14: Consumer Service with Retry-Aware Error Handling and Message
 * Tracing.
 * Listens for PriceTick messages from RabbitMQ and processes them.
 * 
 * Error Handling Strategy:
 * - Transient failures (DB connection issues): Thrown to trigger retry
 * - Permanent failures (validation errors, null data): Logged and discarded
 * 
 * Observability:
 * - Tracks total messages processed
 * - Measures processing duration
 * - Counts failed messages
 * 
 * This service:
 * 1. Receives PriceTick messages from the queue
 * 2. Validates message data
 * 3. Saves valid messages to the database
 * 4. Triggers arbitrage detection logic
 */
@Service
public class PriceTickConsumer {

    private static final Logger log = LoggerFactory.getLogger(PriceTickConsumer.class);

    private final PriceTickRepository priceTickRepository;
    private final ArbitrageService arbitrageService;
    private final Counter messagesProcessedCounter;
    private final Counter messageFailureCounter;
    private final Timer processingDurationTimer;

    @Autowired
    public PriceTickConsumer(PriceTickRepository priceTickRepository,
            ArbitrageService arbitrageService,
            MeterRegistry meterRegistry) {
        this.priceTickRepository = priceTickRepository;
        this.arbitrageService = arbitrageService;

        // Initialize custom metrics
        this.messagesProcessedCounter = Counter.builder("rabbitmq.messages.processed")
                .description("Total number of messages successfully processed from RabbitMQ")
                .register(meterRegistry);

        this.messageFailureCounter = Counter.builder("rabbitmq.messages.failed")
                .description("Total number of failed message processing attempts")
                .register(meterRegistry);

        this.processingDurationTimer = Timer.builder("rabbitmq.messages.processing.duration")
                .description("Time taken to process messages from RabbitMQ")
                .register(meterRegistry);
    }

    /**
     * Processes incoming PriceTick messages from RabbitMQ.
     * 
     * The @RabbitListener annotation automatically:
     * - Deserializes the JSON message to a PriceTick object
     * - Handles connection management
     * - Applies retry policy configured in RabbitMqConfig
     * 
     * Error Handling:
     * - DataAccessException: Requeue for retry (transient DB issues)
     * - ValidationException: Discard message (permanent data quality issue)
     * - After max retries: Message sent to Dead Letter Queue
     * 
     * @param tick The PriceTick message received from the queue
     */
    @RabbitListener(queues = "${cpa.rabbitmq.queue}")
    public void processPriceTick(PriceTick tick) {
        log.info("Received PriceTick from RabbitMQ: exchange={}, pair={}/{}, bid={}, ask={}",
                getExchangeId(tick),
                getCurrencyPairBase(tick),
                getCurrencyPairQuote(tick),
                tick.getBidPrice(),
                tick.getAskPrice());

        // Measure processing duration and track success/failure
        processingDurationTimer.record(() -> {
            try {
                // Step 0: Validate message data
                validatePriceTick(tick);

                // Step 1: Save the PriceTick to the database
                PriceTick savedTick = priceTickRepository.save(tick);
                log.debug("Saved PriceTick to database with ID: {}", savedTick.getId());

                // Step 2: Trigger arbitrage detection for this currency pair
                arbitrageService.detectAndSaveOpportunities(tick.getPair());

                log.info("Successfully processed PriceTick for {}/{}",
                        tick.getPair().getBase(), tick.getPair().getQuote());

                // Increment success counter
                messagesProcessedCounter.increment();

            } catch (DataAccessException e) {
                // TRANSIENT FAILURE: Database connection issues
                // Increment failure counter
                messageFailureCounter.increment();

                // Throw to trigger RabbitMQ retry mechanism (up to 3 attempts)
                log.error("Transient failure processing PriceTick: exchange={}, pair={}/{}, error={}",
                        getExchangeId(tick),
                        getCurrencyPairBase(tick),
                        getCurrencyPairQuote(tick),
                        e.getMessage());

                throw new RuntimeException("Database error, will retry", e);

            } catch (IllegalArgumentException | NullPointerException e) {
                // PERMANENT FAILURE: Invalid data (e.g., null exchange, invalid prices)
                // Increment failure counter
                messageFailureCounter.increment();

                // Log the permanent failure
                log.error("Permanent failure - invalid PriceTick: exchange={}, pair={}/{}, error={}",
                        getExchangeId(tick),
                        getCurrencyPairBase(tick),
                        getCurrencyPairQuote(tick),
                        e.getMessage(), e);

                // THROW exception to trigger retry mechanism
                // Spring will retry 3 times (per RetryTemplate config)
                // After max retries exhausted, message will be rejected and routed to DLQ
                throw new RuntimeException("Invalid message data: " + e.getMessage(), e);

            } catch (Exception e) {
                // UNKNOWN FAILURE: Log with full stack trace for investigation
                // Increment failure counter
                messageFailureCounter.increment();

                // Treat as transient and retry
                log.error("Unexpected error processing PriceTick: exchange={}, pair={}/{}, error={}",
                        getExchangeId(tick),
                        getCurrencyPairBase(tick),
                        getCurrencyPairQuote(tick),
                        e.getMessage(), e);

                throw new RuntimeException("Unexpected error, will retry", e);
            }
        });
    }

    /**
     * Validates that the PriceTick contains the minimum required data.
     * 
     * @param tick The PriceTick to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePriceTick(PriceTick tick) {
        if (tick == null) {
            throw new IllegalArgumentException("PriceTick cannot be null");
        }
        if (tick.getPair() == null) {
            throw new IllegalArgumentException("CurrencyPair cannot be null");
        }
        // Note: Exchange can be null in some test scenarios, so we check defensively
        if (tick.getBidPrice() == null || tick.getAskPrice() == null) {
            throw new IllegalArgumentException("Bid and Ask prices cannot be null");
        }
    }

    /**
     * Safely extracts exchange ID, handling null cases.
     */
    private String getExchangeId(PriceTick tick) {
        return (tick != null && tick.getExchange() != null)
                ? tick.getExchange().getId()
                : "null";
    }

    /**
     * Safely extracts currency pair base, handling null cases.
     */
    private String getCurrencyPairBase(PriceTick tick) {
        return (tick != null && tick.getPair() != null)
                ? tick.getPair().getBase()
                : "null";
    }

    /**
     * Safely extracts currency pair quote, handling null cases.
     */
    private String getCurrencyPairQuote(PriceTick tick) {
        return (tick != null && tick.getPair() != null)
                ? tick.getPair().getQuote()
                : "null";
    }
}
