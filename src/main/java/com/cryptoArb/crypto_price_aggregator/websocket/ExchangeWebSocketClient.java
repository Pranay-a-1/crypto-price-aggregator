package com.cryptoArb.crypto_price_aggregator.websocket;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;

import java.util.function.Consumer;

/**
 * Interface for WebSocket clients that stream price updates.
 * Following Interface Segregation Principle.
 */
public interface ExchangeWebSocketClient {

    /**
     * Connect to the WebSocket stream.
     */
    void connect();

    /**
     * Disconnect from the WebSocket stream.
     */
    void disconnect();

    /**
     * Subscribe to updates for a specific currency pair.
     * @param pair The currency pair to subscribe to
     */
    void subscribe(CurrencyPair pair);

    /**
     * Register a callback for incoming price ticks.
     * @param handler The consumer to handle incoming ticks
     */
    void onMessage(Consumer<PriceTick> handler);
}
