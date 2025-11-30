package com.cryptoArb.crypto_price_aggregator.config;

import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.service.MockPriceFetcher;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Phase 1: Mock Price Fetchers.
 * <p>
 * Following SOLID principles:
 * - Open/Closed: Easy to add new fetchers without modifying PriceServiceImpl
 * - Dependency Inversion: Beans are injected as PriceFetcher abstraction
 * <p>
 * Following YAGNI: Only mock fetchers for Phase 1, real ones come in Phase 4
 */
@Configuration
public class PriceFetcherConfig {

    /**
     * Creates a mock fetcher for Binance exchange.
     */
    @Bean
    public PriceFetcher binanceMockFetcher() {
        return new MockPriceFetcher(Exchange.BINANCE);
    }

    /**
     * Creates a mock fetcher for Coinbase exchange.
     */
    @Bean
    public PriceFetcher coinbaseMockFetcher() {
        return new MockPriceFetcher(Exchange.COINBASE);
    }

    /**
     * Creates a mock fetcher for Kraken exchange.
     */
    @Bean
    public PriceFetcher krakenMockFetcher() {
        return new MockPriceFetcher(Exchange.KRAKEN);
    }
}
