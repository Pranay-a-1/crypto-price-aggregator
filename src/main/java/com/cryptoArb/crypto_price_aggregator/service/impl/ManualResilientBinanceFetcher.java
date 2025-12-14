package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

/**
 * Manual implementation of a Resilient Fetcher (Evolutionary Step).
 * Demonstrates manual retry logic and chaos injection.
 * This fetcher mimics Binance fetching behavior but adds explicit "painful" manual retry loops.
 */
// @Component("manualResilientBinanceFetcher")
public class ManualResilientBinanceFetcher implements PriceFetcher {

    private static final Logger log = LoggerFactory.getLogger(ManualResilientBinanceFetcher.class);
    private final Exchange exchange = Exchange.BINANCE;
    private final String API_URL = "https://api.binance.com/api/v3/ticker/bookTicker?symbol=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    // Configuration for manual retry
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 500;

    @Value("${chaos.latency.min:2000}")
    private long chaosLatencyMin;

    @Value("${chaos.failure.rate:50}")
    private int chaosFailureRate;

    @Autowired
    public ManualResilientBinanceFetcher(RestTemplate restTemplate, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        // Ensure symbol is properly formatted for Binance API
        String base = pair.getBase().toUpperCase();
        String quote = pair.getQuote().toUpperCase();
        String symbol = "USD".equals(quote) ? base + "USDT" : base + quote;

        String url = API_URL + symbol;

        int attempts = 0;
        long backoff = INITIAL_BACKOFF_MS;

        while (attempts <= MAX_RETRIES) {
            try {
                attempts++;
                injectChaos(); // Simulate random failures and latency



                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);

                BigDecimal bid = new BigDecimal(root.get("bidPrice").asText());
                BigDecimal ask = new BigDecimal(root.get("askPrice").asText());

                PriceTick tick = new PriceTick(pair, exchange, bid, ask, Instant.now());
                eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick));
                return tick;

            } catch (Exception e) {
                log.warn("Attempt {} failed for {}: {}", attempts, symbol, e.getMessage());

                if (attempts > MAX_RETRIES) {
                    log.error("All {} attempts failed for {}", attempts, symbol);
                    throw new PriceFetchException("Failed to fetch from Binance after " + MAX_RETRIES + " retries: " + e.getMessage(), e);
                }

                try {
                    log.info("Waiting {}ms before retry...", backoff);
                    Thread.sleep(backoff);
                    backoff *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PriceFetchException("Interrupted during retry backoff", ie);
                }
            }
        }
        throw new PriceFetchException("Unreachable code");
    }

    private void injectChaos() {
        try {
            // Artificial random latency (min 2000ms + random(0-500ms))
            long latency = chaosLatencyMin + random.nextInt(500);
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Random chance of failure
        if (random.nextInt(100) < chaosFailureRate) {
            log.info("CHAOS: Simulating random fetch failure!");
            throw new RuntimeException("Chaos Monkey struck!");
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
