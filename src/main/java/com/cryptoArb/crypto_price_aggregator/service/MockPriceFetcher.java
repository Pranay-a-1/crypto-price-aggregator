package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;

/**
 * Mock implementation of PriceFetcher for Phase 1.
 * Generates random prices for testing without external dependencies.
 * <p>
 * Following SOLID principles:
 * - Single Responsibility: Only generates mock price ticks
 * - Liskov Substitution: Can be substituted for any PriceFetcher
 * <p>
 * Following KISS: Simple random number generation
 * Following YAGNI: No real HTTP calls yet (Phase 4)
 */
// @Component // Phase 4: Removed to replace with real fetchers
public class MockPriceFetcher implements PriceFetcher {

    private final Exchange exchange;
    private final Random random;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Constructor with dependency injection.
     *
     * @param exchange The exchange this fetcher represents
     */
    public MockPriceFetcher(Exchange exchange, ApplicationEventPublisher eventPublisher) {
        this.exchange = exchange;
        this.random = new Random();
        this.eventPublisher = eventPublisher;
    }

    public MockPriceFetcher(Exchange exchange) {
        this(exchange, null);
    }

    /**
     * Default constructor for Spring (creates MOCK exchange).
     */
    public MockPriceFetcher() {
        this(Exchange.MOCK , null);
    }

    @Override
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        if (pair == null) {
            throw new PriceFetchException("CurrencyPair cannot be null");
        }

        try {
            // Generate random base price between 1000 and 100000
            double basePrice = 1000 + random.nextDouble() * 99000;

            // Bid is slightly lower than base (0.1% to 0.5% below)
            double bidOffset = basePrice * (0.001 + random.nextDouble() * 0.004);
            BigDecimal bid = BigDecimal.valueOf(basePrice - bidOffset)
                    .setScale(2, RoundingMode.HALF_UP);

            // Ask is slightly higher than base (0.1% to 0.5% above)
            double askOffset = basePrice * (0.001 + random.nextDouble() * 0.004);
            BigDecimal ask = BigDecimal.valueOf(basePrice + askOffset)
                    .setScale(2, RoundingMode.HALF_UP);

            PriceTick tick = new PriceTick(pair, exchange, bid, ask, Instant.now());
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick));
            }
            return tick;

        } catch (Exception e) {
            throw new PriceFetchException(
                    "Failed to fetch mock price for " + pair + " from " + exchange, e);
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
