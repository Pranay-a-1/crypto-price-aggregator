package com.cryptoArb.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Phase 14: Dead Letter Queue Listener.
 * 
 * TDD GREEN PHASE: Implements DLQ message handling.
 * 
 * This service listens for messages that have failed after max retries
 * and been routed to the Dead Letter Queue. It:
 * - Logs comprehensive failure information
 * - Extracts retry metadata from message headers
 * - Emits metrics for monitoring
 * - Demonstrates production-grade error handling
 * 
 * Design Decisions:
 * - Logging only (no database persistence) for simplicity
 * - Could be extended to store in failed_messages table
 * - Metrics allow alerting on DLQ message rate
 * 
 * Why This Matters:
 * - Production systems must handle failures gracefully
 * - DLQ prevents message loss and enables investigation
 * - Metrics/logging provide observability into data quality issues
 */
@Service
public class DeadLetterQueueListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueListener.class);

    private final Counter dlqMessageCounter;

    @Autowired
    public DeadLetterQueueListener(MeterRegistry meterRegistry) {
        // Create custom metric for DLQ messages
        this.dlqMessageCounter = Counter.builder("rabbitmq.dlq.messages")
                .description("Total number of messages received in Dead Letter Queue")
                .tag("queue", "price.tick.dlq")
                .register(meterRegistry);
    }

    /**
     * Listens for messages in the Dead Letter Queue.
     * 
     * These are messages that have:
     * 1. Failed processing in PriceTickConsumer
     * 2. Been retried 3 times (per RetryTemplate config)
     * 3. Been rejected and routed to DLQ via x-dead-letter-exchange
     * 
     * Error Handling Strategy:
     * - Log comprehensive failure details for investigation
     * - Increment DLQ metrics for monitoring/alerting
     * - Do NOT throw exceptions (we don't want to retry DLQ messages)
     * 
     * Message Headers Available:
     * - x-death: Array of death events (retry history)
     * - x-first-death-reason: Original exception that caused failure
     * - x-first-death-queue: Original queue name
     * - x-first-death-exchange: Original exchange name
     * 
     * @param message The failed message with headers
     */
    @RabbitListener(queues = "${cpa.rabbitmq.dlq.queue}")
    public void handleFailedMessage(Message message) {
        try {
            // Extract message metadata
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            Map<String, Object> headers = message.getMessageProperties().getHeaders();

            // Log comprehensive failure information
            log.error("=== DEAD LETTER QUEUE: Message Processing Failed ===");
            log.error("Message Body: {}", messageBody);
            log.error("Message ID: {}", message.getMessageProperties().getMessageId());
            log.error("Timestamp: {}", message.getMessageProperties().getTimestamp());

            // Extract death information (retry history)
            Object deathInfo = headers.get("x-death");
            if (deathInfo != null) {
                log.error("Death Info (Retry History): {}", deathInfo);
            }

            // Extract original failure reason
            Object firstDeathReason = headers.get("x-first-death-reason");
            if (firstDeathReason != null) {
                log.error("Original Failure Reason: {}", firstDeathReason);
            }

            // Extract original queue
            Object firstDeathQueue = headers.get("x-first-death-queue");
            if (firstDeathQueue != null) {
                log.error("Original Queue: {}", firstDeathQueue);
            }

            // Log all headers for debugging
            log.error("All Headers: {}", headers);
            log.error("==================================================");

            // Increment DLQ metric
            dlqMessageCounter.increment();

            // NOTE: In a production system, you might:
            // - Store message in a failed_messages table for analysis
            // - Send alert to PagerDuty/Slack if DLQ rate exceeds threshold
            // - Attempt manual correction (e.g., fix data and republish)

        } catch (Exception e) {
            // Log error but DO NOT throw - we don't want DLQ messages to be requeued
            log.error("Error processing DLQ message (will not retry): ", e);
        }
    }

    /**
     * Alternative Implementation: Database Persistence
     * 
     * If storing failed messages in database:
     * 
     * @Entity
     *         class FailedMessage {
     * @Id Long id;
     *     String messageBody;
     *     String failureReason;
     *     Instant failedAt;
     *     String originalQueue;
     *     int retryCount;
     *     }
     * 
     *     Then in handleFailedMessage():
     *     FailedMessage failed = new FailedMessage();
     *     failed.setMessageBody(messageBody);
     *     failed.setFailureReason(extractReason(headers));
     *     failedMessageRepository.save(failed);
     * 
     *     Trade-offs:
     *     - Pros: Enables analysis, potential replay, audit trail
     *     - Cons: Additional database writes, storage growth
     * 
     *     Current choice: Logging + Metrics (simpler, sufficient for most cases)
     */
}
