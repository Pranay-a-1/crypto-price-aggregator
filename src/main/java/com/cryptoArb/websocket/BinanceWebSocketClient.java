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
public class BinanceWebSocketClient extends ExchangeWebSocketClient {

    // Binance stream for BTCUSDT ticker
    private static final String WS_URL = "wss://stream.binance.com:9443/ws/btcusdt@ticker";
    private static final String TOPIC_NAME = "price.tick";

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected String getWebSocketUrl() {
        return WS_URL;
    }

    @Override
    protected String getSubscriptionMessage() {
        // Binance stream URL already includes subscription, no need to send message
        return null;
    }

    @Override
    protected PriceTick parseMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            // Binance ticker event type is "24hrTicker" but usually "e" field
            if (node.has("e") && "24hrTicker".equals(node.get("e").asText())) {
                // String symbol = node.get("s").asText(); // BTCUSDT - unused
                String bidPriceStr = node.get("b").asText();
                String askPriceStr = node.get("a").asText();
                long eventTime = node.get("E").asLong();

                // Parse currency pair (Assuming BTCUSDT)
                // In a real app, we'd need more robust parsing or mapping
                CurrencyPair pair = new CurrencyPair("BTC", "USD"); // Mapping USDT to USD for simplicity/arbitrage

                return new PriceTick(
                        pair,
                        new Exchange("binance"),
                        Instant.ofEpochMilli(eventTime),
                        new BigDecimal(bidPriceStr),
                        new BigDecimal(askPriceStr));
            }
        } catch (Exception e) {
            log.warn("Failed to parse Binance message: {}", message, e);
        }
        return null;
    }

    @Override
    public String getExchangeName() {
        return "Binance";
    }

    @Override
    protected String getTopicName() {
        return TOPIC_NAME;
    }
}
