package com.cryptoArb.websocket;

import com.cryptoArb.domain_spring.PriceTick;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CoinbaseWebSocketClientTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CoinbaseWebSocketClient client;

    @Test
    void parseMessage_ValidTicker_ReturnsPriceTick() {
        String json = "{" +
                "\"type\": \"ticker\"," +
                "\"product_id\": \"BTC-USD\"," +
                "\"price\": \"50000.00\"," +
                "\"best_bid\": \"49999.00\"," +
                "\"best_ask\": \"50001.00\"," +
                "\"time\": \"2024-01-01T12:00:00.000000Z\"" +
                "}";

        PriceTick tick = client.parseMessage(json);

        assertNotNull(tick);
        assertEquals("BTC", tick.getPair().getBase());
        assertEquals("USD", tick.getPair().getQuote());
        assertEquals("coinbase", tick.getExchange().getId());
        assertEquals(new BigDecimal("49999.00"), tick.getBidPrice());
        assertEquals(new BigDecimal("50001.00"), tick.getAskPrice());
    }

    @Test
    void parseMessage_InvalidType_ReturnsNull() {
        String json = "{\"type\": \"heartbeat\"}";
        assertNull(client.parseMessage(json));
    }

    @Test
    void parseMessage_MalformedJson_ReturnsNull() {
        String json = "{invalid-json}";
        assertNull(client.parseMessage(json));
    }
}
