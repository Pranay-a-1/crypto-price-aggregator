package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Professional implementation of Binance Fetcher (Phase 4).
 * Uses injected RestTemplate and ObjectMapper.
 */
@Component
public class BinanceFetcher implements PriceFetcher {

    private final Exchange exchange = Exchange.BINANCE;
    private final String API_URL = "https://api.binance.com/api/v3/ticker/bookTicker?symbol=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public BinanceFetcher(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        String symbol = pair.getBase() + pair.getQuote(); // e.g., BTCUSDT
        // Binance usually uses USDT instead of USD. Handling simple case here.
        if ("USD".equals(pair.getQuote())) {
            symbol = pair.getBase() + "USDT";
        }

        String url = API_URL + symbol;

        try {
            // Artificial latency
            Thread.sleep(500);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            BigDecimal bid = new BigDecimal(root.get("bidPrice").asText());
            BigDecimal ask = new BigDecimal(root.get("askPrice").asText());

            return new PriceTick(pair, exchange, bid, ask, Instant.now());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceFetchException("Interrupted while fetching from Binance", e);
        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch from Binance: " + e.getMessage(), e);
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
