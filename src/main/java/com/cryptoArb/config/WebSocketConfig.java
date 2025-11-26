package com.cryptoArb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketConfig {

    @Bean
    public WebSocketClient webSocketClient() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        // Tyrus specific configuration could go here if needed
        // client.getUserProperties().put("org.glassfish.tyrus.client.ClientManager.WLS_MAX_IDLE_TIMEOUT_MS",
        // 30000);
        return client;
    }
}
