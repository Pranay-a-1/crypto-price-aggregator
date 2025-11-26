package com.cryptoArb.websocket;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketRateLimiter {
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public WebSocketRateLimiter() {
        RateLimiterConfig coinbaseConfig = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO) // Fail immediately if limit exceeded
                .build();

        RateLimiterConfig binanceConfig = RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();

        limiters.put("COINBASE", registry.rateLimiter("coinbase", coinbaseConfig));
        limiters.put("BINANCE", registry.rateLimiter("binance", binanceConfig));
    }

    public boolean tryAcquire(String exchange) {
        RateLimiter limiter = limiters.get(exchange);
        if (limiter == null)
            return true; // No limit if not configured
        return limiter.acquirePermission();
    }
}
