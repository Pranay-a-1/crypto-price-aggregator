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
 * Professional implementation of Binance Fetcher (Phase 4 & 7).
 * Uses injected RestTemplate and ObjectMapper.
 * Enhanced with Resilience4j (Circuit Breaker & Retry).
 */
@Component
public class BinanceFetcher implements PriceFetcher {

    private final Exchange exchange = Exchange.BINANCE;
    private final String API_URL = "https://api.binance.com/api/v3/ticker/bookTicker?symbol=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    @Value("${chaos.mode.enabled:false}")
    private boolean chaosModeEnabled;

    @Value("${chaos.latency.min:2000}")
    private long chaosLatencyMin;

    @Value("${chaos.failure.rate:50}")
    private int chaosFailureRate;

    @Autowired
    public BinanceFetcher(RestTemplate restTemplate, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @CircuitBreaker(name = "binance")
    @Retry(name = "binance")
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        if (chaosModeEnabled) {
            injectChaos();
        }
        // Ensure symbol is properly formatted for Binance API
        // Binance requires uppercase letters only and specific format
        String base = pair.getBase().toUpperCase();
        String quote = pair.getQuote().toUpperCase();

        // Binance usually uses USDT instead of USD
        String symbol;
        if ("USD".equals(quote)) {
            symbol = base + "USDT";
        } else {
            symbol = base + quote;
        }

        // Validate symbol format to match Binance requirements: ^[A-Z0-9-_.]{1,20}$
        if (!symbol.matches("^[A-Z0-9-_.]{1,20}$")) {
            throw new PriceFetchException("Invalid symbol format for Binance: " + symbol);
        }

        String url = API_URL + symbol;

        try {


            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            BigDecimal bid = new BigDecimal(root.get("bidPrice").asText());
            BigDecimal ask = new BigDecimal(root.get("askPrice").asText());

            PriceTick tick = new PriceTick(pair, exchange, bid, ask, Instant.now());
            eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick));
            return tick;

        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch from Binance: " + e.getMessage(), e);
        }
    }



    private void injectChaos() throws PriceFetchException {
        try {
            // Artificial random latency (min + random(0-500ms))
            long latency = chaosLatencyMin + random.nextInt(500);
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Random chance of failure
        if (random.nextInt(100) < chaosFailureRate) {
            throw new PriceFetchException("Chaos Monkey: Simulating failure for Binance");
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
