package com.cryptoArb.crypto_price_aggregator.config;

import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.websocket.ExchangeWebSocketClient;
import com.cryptoArb.crypto_price_aggregator.websocket.impl.MockWebSocketClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class WebSocketConfig {

    @Bean
    @Profile("!prod") // Use mock for dev/test
    public ExchangeWebSocketClient binanceWebSocketClient() {
        return new MockWebSocketClient(Exchange.BINANCE);
    }

    @Bean
    @Profile("!prod")
    public ExchangeWebSocketClient coinbaseWebSocketClient() {
        return new MockWebSocketClient(Exchange.COINBASE);
    }
}
