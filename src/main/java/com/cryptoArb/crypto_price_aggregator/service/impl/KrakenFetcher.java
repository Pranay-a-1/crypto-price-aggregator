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
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Professional implementation of Kraken Fetcher.
 * Uses injected RestTemplate and ObjectMapper.
 * Enhanced with Resilience4j.
 */
@Component
public class KrakenFetcher implements PriceFetcher {

    private final Exchange exchange = Exchange.KRAKEN;
    private static final String API_URL_TEMPLATE = "https://api.kraken.com/0/public/Ticker?pair=%s";

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
    public KrakenFetcher(RestTemplate restTemplate, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @CircuitBreaker(name = "kraken")
    @Retry(name = "kraken")
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        if (chaosModeEnabled) {
            injectChaos();
        }
        // Kraken uses XBT for BTC
        String base = pair.getBase();
        if ("BTC".equals(base)) {
            base = "XBT";
        }
        String quote = pair.getQuote(); // Kraken usually handles USD ok, but returns ZUSD

        String symbol = base + quote; // e.g., XBTUSD
        String url = String.format(API_URL_TEMPLATE, symbol);

        try {


            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error") && root.get("error").size() > 0) {
                throw new PriceFetchException("Kraken API Error: " + root.get("error").toString());
            }

            JsonNode result = root.get("result");
            // The key in result is dynamic, e.g., XXBTZUSD
            String key = result.fieldNames().next();
            JsonNode ticker = result.get(key);


            // Kraken returns "b" (bid) and "a" (ask) as arrays: [price, wholeLotVolume, lotVolume]
            // We need index 0 for price
            BigDecimal bid = new BigDecimal(ticker.get("b").get(0).asText());
            BigDecimal ask = new BigDecimal(ticker.get("a").get(0).asText());

            PriceTick tick = new PriceTick(pair, exchange, bid, ask, Instant.now());
            eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick));
            return tick;


        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch from Kraken: " + e.getMessage(), e);
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
            throw new PriceFetchException("Chaos Monkey: Simulating failure for Kraken");
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
