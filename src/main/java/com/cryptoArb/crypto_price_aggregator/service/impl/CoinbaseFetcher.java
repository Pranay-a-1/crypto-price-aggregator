package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

/**
 * Professional implementation of Coinbase Fetcher (Phase 4 & 7).
 * Uses injected RestTemplate and ObjectMapper.
 * Enhanced with Resilience4j.
 */
@Component
public class CoinbaseFetcher implements PriceFetcher {

    private final Exchange exchange = Exchange.COINBASE;
    // Updated to Coinbase Exchange API (formerly GDAX/Pro) for Ticker
    private static final String API_URL_TEMPLATE = "https://api.exchange.coinbase.com/products/%s/ticker";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    @Autowired
    public CoinbaseFetcher(RestTemplate restTemplate, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Value("${chaos.mode.enabled:false}")
    private boolean chaosModeEnabled;


    @Value("${chaos.latency.min:2000}")
    private long chaosLatencyMin;

    @Value("${chaos.failure.rate:50}")
    private int chaosFailureRate;

    @Override
    @CircuitBreaker(name = "coinbase")
    @Retry(name = "coinbase")
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        if (chaosModeEnabled) {
            injectChaos();
        }

        // Coinbase uses hyphen: BTC-USD
        String symbol = pair.getBase() + "-" + pair.getQuote();
        String url = String.format(API_URL_TEMPLATE, symbol);

        try {

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Parse separate bid/ask and time from Exchange API response
            BigDecimal bid = new BigDecimal(root.get("bid").asText());
            BigDecimal ask = new BigDecimal(root.get("ask").asText());

            Instant timestamp;
            if (root.has("time")) {
                timestamp = Instant.parse(root.get("time").asText());
            } else {
                timestamp = Instant.now();
            }

            PriceTick tick = new PriceTick(pair, exchange, bid, ask, timestamp);
            eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick));
            return tick;
        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch from Coinbase: " + e.getMessage(), e);
        }
    }

    private void injectChaos() throws PriceFetchException {
        try {
            long latency = chaosLatencyMin + random.nextInt(500);
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (random.nextInt(100) < chaosFailureRate) {
            throw new PriceFetchException("Chaos Monkey: Simulating failure for Coinbase");
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
