package com.cryptoArb.websocket;

import com.cryptoArb.domain_spring.PriceTick;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class BinanceWebSocketClientTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BinanceWebSocketClient client;

    @Test
    void parseMessage_ValidTicker_ReturnsPriceTick() {
        String json = "{" +
                "\"e\": \"24hrTicker\"," +
                "\"s\": \"BTCUSDT\"," +
                "\"b\": \"50000.00\"," +
                "\"a\": \"50001.00\"," +
                "\"E\": 1672531200000" +
                "}";

        PriceTick tick = client.parseMessage(json);

        assertNotNull(tick);
        assertEquals("BTC", tick.getPair().getBase());
        assertEquals("USD", tick.getPair().getQuote());
        assertEquals("binance", tick.getExchange().getId());
        assertEquals(new BigDecimal("50000.00"), tick.getBidPrice());
        assertEquals(new BigDecimal("50001.00"), tick.getAskPrice());
    }

    @Test
    void parseMessage_InvalidEvent_ReturnsNull() {
        String json = "{\"e\": \"trade\"}";
        assertNull(client.parseMessage(json));
    }
}
