package com.cryptoArb.websocket;

import com.cryptoArb.domain_spring.PriceTick;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class ExchangeWebSocketClient implements WebSocketHandler {

    @Autowired
    protected WebSocketClient webSocketClient;

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @Autowired
    protected WebSocketRateLimiter rateLimiter;

    protected WebSocketSession session;
    protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    protected Instant lastMessageTime = Instant.now();
    protected AtomicBoolean isConnected = new AtomicBoolean(false);
    protected AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_DELAY = 60_000; // 60 seconds

    protected abstract String getWebSocketUrl();

    protected abstract String getSubscriptionMessage();

    protected abstract PriceTick parseMessage(String message);

    public abstract String getExchangeName();

    protected abstract String getTopicName();

    @PostConstruct
    public void connect() {
        if (isShuttingDown.get())
            return;

        log.info("Connecting to {} WebSocket...", getExchangeName());
        try {
            webSocketClient.execute(this, getWebSocketUrl());
        } catch (Exception e) {
            log.error("Failed to connect to {}", getExchangeName(), e);
            reconnect();
        }
    }

    protected void reconnect() {
        if (isShuttingDown.get())
            return;

        int delay = Math.min(1000 * (int) Math.pow(2, reconnectAttempts), MAX_RECONNECT_DELAY);
        log.info("Scheduling reconnection to {} in {} ms (Attempt {})", getExchangeName(), delay,
                reconnectAttempts + 1);

        scheduler.schedule(() -> {
            reconnectAttempts++;
            connect();
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connected to {} WebSocket", getExchangeName());
        this.session = session;
        this.isConnected.set(true);
        this.reconnectAttempts = 0;
        this.lastMessageTime = Instant.now();

        String subscriptionMessage = getSubscriptionMessage();
        if (subscriptionMessage != null && !subscriptionMessage.isEmpty()) {
            sendMessage(subscriptionMessage);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
            throws Exception {
        lastMessageTime = Instant.now();
        if (message instanceof TextMessage textMessage) {
            try {
                if (!rateLimiter.tryAcquire(getExchangeName().toUpperCase())) {
                    // Rate limit exceeded, drop message
                    return;
                }

                String payload = textMessage.getPayload();
                PriceTick tick = parseMessage(payload);
                if (tick != null) {
                    rabbitTemplate.convertAndSend(getTopicName(), tick);
                }
            } catch (Exception e) {
                log.error("Error processing message from {}", getExchangeName(), e);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error in {} WebSocket", getExchangeName(), exception);
        // Connection usually closes after transport error, handled in
        // afterConnectionClosed
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
            throws Exception {
        log.warn("Disconnected from {} WebSocket: {}", getExchangeName(), closeStatus);
        this.isConnected.set(false);
        this.session = null;
        if (!isShuttingDown.get()) {
            reconnect();
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    protected void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Failed to send message to {}", getExchangeName(), e);
            }
        }
    }

    public boolean isHealthy() {
        return isConnected.get() && Duration.between(lastMessageTime, Instant.now()).toSeconds() < 60;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down {} WebSocket client", getExchangeName());
        isShuttingDown.set(true);

        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("Error closing session for {}", getExchangeName(), e);
            }
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Heartbeat check
    @PostConstruct
    public void initHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isConnected.get() && Duration.between(lastMessageTime, Instant.now()).toSeconds() > 60) {
                log.warn("No message from {} in 60s, reconnecting...", getExchangeName());
                try {
                    if (session != null)
                        session.close();
                } catch (IOException e) {
                    // ignore
                }
                // Reconnect will be triggered by afterConnectionClosed
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
