package com.cryptoArb.crypto_price_aggregator.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple token bucket rate limiter for WebSocket messages.
 * Prevents flooding the system with too many updates.
 */
@Component
public class WebSocketRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(WebSocketRateLimiter.class);
    private static final int MAX_MESSAGES_PER_SECOND = 100;

    private final AtomicInteger messageCount = new AtomicInteger(0);
    private volatile long lastResetTime = System.currentTimeMillis();

    /**
     * Checks if a message should be processed based on current rate.
     * @return true if allowed, false if rate limited
     */
    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - lastResetTime > 1000) {
            synchronized (this) {
                if (now - lastResetTime > 1000) {
                    messageCount.set(0);
                    lastResetTime = now;
                }
            }
        }

        if (messageCount.incrementAndGet() <= MAX_MESSAGES_PER_SECOND) {
            return true;
        } else {
            log.trace("Rate limit exceeded for WebSocket messages");
            return false;
        }
    }
}
