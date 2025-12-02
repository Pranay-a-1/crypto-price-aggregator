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
 * Professional implementation of Coinbase Fetcher (Phase 4).
 * Uses injected RestTemplate and ObjectMapper.
 */
@Component
public class CoinbaseFetcher implements PriceFetcher {

    private final Exchange exchange = Exchange.COINBASE;
    // URL: https://api.exchange.coinbase.com/products/{symbol}/ticker
    private final String API_URL_TEMPLATE = "https://api.exchange.coinbase.com/products/%s/ticker";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CoinbaseFetcher(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        // Coinbase uses hyphen: BTC-USD
        String symbol = pair.getBase() + "-" + pair.getQuote();
        String url = String.format(API_URL_TEMPLATE, symbol);

        try {
            // Artificial latency
            Thread.sleep(500);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Coinbase returns "bid" and "ask"
            BigDecimal bid = new BigDecimal(root.get("bid").asText());
            BigDecimal ask = new BigDecimal(root.get("ask").asText());

            return new PriceTick(pair, exchange, bid, ask, Instant.now());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceFetchException("Interrupted while fetching from Coinbase", e);
        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch from Coinbase: " + e.getMessage(), e);
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
