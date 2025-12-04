package com.cryptoArb.crypto_price_aggregator.health;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Health Indicator for Exchange Fetchers
 *
 * Phase 8 - Observability:
 * - Checks health of all configured exchange fetchers
 * - Returns UP if all exchanges are reachable
 * - Returns DOWN if any exchange is unreachable
 * - Provides detailed status for each exchange
 *
 * Why Custom: Standard health checks don't monitor external dependencies
 * This enables monitoring of critical exchange connectivity
 *
 * Design Decisions:
 * - Uses lightweight test currency pair (BTC/USDT) for health check
 * - Catches exceptions to prevent cascading failures
 * - Provides granular details for each exchange status
 * - Follows fail-fast principle: any single exchange failure = DOWN
 */
@Slf4j
@Component
public class ExchangeHealthIndicator implements HealthIndicator {

    private final List<PriceFetcher> fetchers;
    private static final CurrencyPair HEALTH_CHECK_PAIR = new CurrencyPair("BTC", "USDT");

    public ExchangeHealthIndicator(List<PriceFetcher> fetchers) {
        this.fetchers = fetchers;
        log.info("ExchangeHealthIndicator initialized with {} fetchers", fetchers.size());
    }

    @Override
    public Health health() {
        if (fetchers == null || fetchers.isEmpty()) {
            return Health.up()
                    .withDetail("message", "No exchanges configured")
                    .build();
        }

        Map<String, String> exchangeStatuses = new HashMap<>();
        boolean allHealthy = true;

        for (PriceFetcher fetcher : fetchers) {
            String exchangeName = fetcher.getExchange().getDisplayName();
            try {
                // Attempt lightweight health check
                // Note: This is a simplified check. In production, you might want
                // to use a dedicated health endpoint or ping mechanism
                fetcher.fetchPrice(HEALTH_CHECK_PAIR);
                exchangeStatuses.put(exchangeName, "UP");
                log.debug("Exchange {} is healthy", exchangeName);
            } catch (Exception e) {
                exchangeStatuses.put(exchangeName, "DOWN: " + e.getMessage());
                allHealthy = false;
                log.warn("Exchange {} health check failed: {}", exchangeName, e.getMessage());
            }
        }

        Health.Builder healthBuilder = allHealthy ? Health.up() : Health.down();

        return healthBuilder
                .withDetail("exchanges", exchangeStatuses)
                .withDetail("totalExchanges", fetchers.size())
                .withDetail("healthyExchanges", exchangeStatuses.values().stream()
                        .filter(status -> status.equals("UP"))
                        .count())
                .build();
    }
}
