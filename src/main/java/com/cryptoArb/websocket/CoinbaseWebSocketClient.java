package com.cryptoArb.websocket;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
public class CoinbaseWebSocketClient extends ExchangeWebSocketClient {

    private static final String WS_URL = "wss://ws-feed.exchange.coinbase.com";
    private static final String TOPIC_NAME = "price.tick"; // Assuming this is the topic name from Phase 14

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected String getWebSocketUrl() {
        return WS_URL;
    }

    @Override
    protected String getSubscriptionMessage() {
        return "{" +
                "\"type\": \"subscribe\"," +
                "\"product_ids\": [\"BTC-USD\"]," +
                "\"channels\": [\"ticker\"]" +
                "}";
    }

    @Override
    protected PriceTick parseMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String type = node.path("type").asText();

            if ("ticker".equals(type)) {
                String productId = node.path("product_id").asText();
                String priceStr = node.path("price").asText();
                String bestBidStr = node.path("best_bid").asText();
                String bestAskStr = node.path("best_ask").asText();
                String timeStr = node.path("time").asText();

                if (priceStr.isEmpty() || bestBidStr.isEmpty() || bestAskStr.isEmpty()) {
                    return null;
                }

                // Parse currency pair
                String[] parts = productId.split("-");
                CurrencyPair pair = new CurrencyPair(parts[0], parts[1]);

                return new PriceTick(
                        pair,
                        new Exchange("coinbase"),
                        Instant.parse(timeStr),
                        new BigDecimal(bestBidStr),
                        new BigDecimal(bestAskStr));
            }
        } catch (Exception e) {
            log.warn("Failed to parse Coinbase message: {}", message, e);
        }
        return null;
    }

    @Override
    public String getExchangeName() {
        return "Coinbase";
    }

    @Override
    protected String getTopicName() {
        return TOPIC_NAME;
    }
}
