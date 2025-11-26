package com.cryptoArb.observability.actuator;

import com.cryptoArb.websocket.ExchangeWebSocketClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebSocketHealthIndicator implements HealthIndicator {

    private final List<ExchangeWebSocketClient> clients;

    public WebSocketHealthIndicator(List<ExchangeWebSocketClient> clients) {
        this.clients = clients;
    }

    @Override
    public Health health() {
        Map<String, String> exchangeStatuses = clients.stream()
                .collect(Collectors.toMap(
                        ExchangeWebSocketClient::getExchangeName,
                        client -> client.isHealthy() ? "UP" : "DOWN"));

        boolean allHealthy = clients.stream().allMatch(ExchangeWebSocketClient::isHealthy);

        if (allHealthy) {
            return Health.up()
                    .withDetail("exchanges", exchangeStatuses)
                    .build();
        }
        return Health.down()
                .withDetail("exchanges", exchangeStatuses)
                .build();
    }
}
