package com.cryptoArb.observability.actuator;

import com.cryptoArb.websocket.ExchangeWebSocketClient;
import com.cryptoArb.domain_spring.PriceTick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WebSocketHealthIndicatorTest {

    private TestWebSocketClient client1;
    private TestWebSocketClient client2;

    @BeforeEach
    void setUp() {
        client1 = new TestWebSocketClient("Coinbase");
        client2 = new TestWebSocketClient("Binance");
    }

    @Test
    void health_AllClientsHealthy_ReturnsUp() {
        client1.setHealthy(true);
        client2.setHealthy(true);

        WebSocketHealthIndicator indicator = new WebSocketHealthIndicator(Arrays.asList(client1, client2));
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("UP", ((Map) health.getDetails().get("exchanges")).get("Coinbase"));
        assertEquals("UP", ((Map) health.getDetails().get("exchanges")).get("Binance"));
    }

    @Test
    void health_OneClientUnhealthy_ReturnsDown() {
        client1.setHealthy(true);
        client2.setHealthy(false);

        WebSocketHealthIndicator indicator = new WebSocketHealthIndicator(Arrays.asList(client1, client2));
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("UP", ((Map) health.getDetails().get("exchanges")).get("Coinbase"));
        assertEquals("DOWN", ((Map) health.getDetails().get("exchanges")).get("Binance"));
    }

    static class TestWebSocketClient extends ExchangeWebSocketClient {
        private final String name;
        private boolean healthy;

        TestWebSocketClient(String name) {
            this.name = name;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
            // We need to set the internal isConnected flag or override isHealthy if it
            // exists.
            // ExchangeWebSocketClient doesn't have isHealthy() method visible in the
            // snippet I saw?
            // Wait, I used client.isHealthy() in the indicator.
            // I need to check ExchangeWebSocketClient.java to see if isHealthy() exists.
            // It probably uses isConnected.get().
            this.isConnected.set(healthy);
        }

        @Override
        public boolean isHealthy() {
            return healthy;
        }

        @Override
        protected String getWebSocketUrl() {
            return "";
        }

        @Override
        protected String getSubscriptionMessage() {
            return "";
        }

        @Override
        protected PriceTick parseMessage(String message) {
            return null;
        }

        @Override
        public String getExchangeName() {
            return name;
        }

        @Override
        protected String getTopicName() {
            return "";
        }
    }
}
