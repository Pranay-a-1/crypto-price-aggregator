package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.cryptoArb.crypto_price_aggregator.service.PriceFetcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

/**
 * Professional implementation of Kraken Fetcher.
 * Uses injected RestTemplate and ObjectMapper.
 */
@Component
public class KrakenFetcher implements PriceFetcher {

    private final Exchange exchange = Exchange.KRAKEN;
    private final String API_URL_TEMPLATE = "https://api.kraken.com/0/public/Ticker?pair=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public KrakenFetcher(RestTemplate restTemplate, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException {
        // Kraken uses XBT for BTC
        String base = pair.getBase();
        if ("BTC".equals(base)) {
            base = "XBT";
        }

        String symbol = base + pair.getQuote();
        String url = String.format(API_URL_TEMPLATE, symbol);

        try {
            // Artificial latency to match other fetchers
            Thread.sleep(500);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            JsonNode errorNode = root.get("error");
            if (errorNode != null && errorNode.isArray() && !errorNode.isEmpty()) {
                throw new PriceFetchException("Kraken API returned error: " + errorNode.toString());
            }

            JsonNode resultNode = root.get("result");
            if (resultNode == null || resultNode.isEmpty()) {
                throw new PriceFetchException("Kraken API returned empty result");
            }

            // The key in 'result' is dynamic (e.g., XXBTZUSD), so we take the first field
            Iterator<Map.Entry<String, JsonNode>> fields = resultNode.fields();
            if (!fields.hasNext()) {
                throw new PriceFetchException("Kraken API result contains no ticker data");
            }

            JsonNode tickerData = fields.next().getValue();

            // Kraken returns "b" (bid) and "a" (ask) as arrays: [price, wholeLotVolume, lotVolume]
            // We need index 0 for price
            BigDecimal bid = new BigDecimal(tickerData.get("b").get(0).asText());
            BigDecimal ask = new BigDecimal(tickerData.get("a").get(0).asText());

            PriceTick tick = new PriceTick(pair, exchange, bid, ask, Instant.now());
            eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick));
            return tick;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceFetchException("Interrupted while fetching from Kraken", e);
        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch from Kraken: " + e.getMessage(), e);
        }
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }
}
