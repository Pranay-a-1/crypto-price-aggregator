package com.cryptoArb.crypto_price_aggregator.websocket.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.websocket.ExchangeWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Mock WebSocket client that simulates streaming prices.
 * Used to demonstrate architecture without external dependencies.
 */
public class MockWebSocketClient implements ExchangeWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(MockWebSocketClient.class);
    private final Exchange exchange;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Consumer<PriceTick> messageHandler;
    private final Random random = new Random();
    private volatile boolean connected = false;

    public MockWebSocketClient(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void connect() {
        log.info("Connecting to {} WebSocket...", exchange);
        connected = true;
        // Simulate stream
        executor.scheduleAtFixedRate(this::generateMockTick, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void disconnect() {
        log.info("Disconnecting from {} WebSocket...", exchange);
        connected = false;
        executor.shutdown();
    }

    @Override
    public void subscribe(CurrencyPair pair) {
        log.info("Subscribed to {} on {}", pair, exchange);
    }

    @Override
    public void onMessage(Consumer<PriceTick> handler) {
        this.messageHandler = handler;
    }

    private void generateMockTick() {
        if (!connected || messageHandler == null) return;

        // Generate a mock tick for BTC/USD
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        BigDecimal bid = new BigDecimal(50000 + random.nextInt(100));
        BigDecimal ask = bid.add(new BigDecimal(10 + random.nextInt(10)));

        PriceTick tick = new PriceTick(
                pair,
                exchange,
                bid,
                ask,
                Instant.now()
        );

        messageHandler.accept(tick);
    }
}
