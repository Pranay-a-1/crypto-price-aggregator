package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Unit test for BinanceFetcher using MockRestServiceServer.
 */
@ExtendWith(MockitoExtension.class)
class BinanceFetcherTest {

    private BinanceFetcher fetcher;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper objectMapper = new ObjectMapper();
        fetcher = new BinanceFetcher(restTemplate, objectMapper);
    }

    @Test
    void testFetchPrice_Success() throws Exception {
        String jsonResponse = "{\"symbol\": \"BTCUSDT\", \"bidPrice\": \"40000.00000000\", \"bidQty\": \"10.00000000\", \"askPrice\": \"40000.01000000\", \"askQty\": \"10.00000000\"}";

        mockServer.expect(requestTo("https://api.binance.com/api/v3/ticker/bookTicker?symbol=BTCUSDT"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertNotNull(tick);
        assertEquals(Exchange.BINANCE, tick.getExchange());
        assertEquals(new BigDecimal("40000.00000000"), tick.getBid());
        assertEquals(new BigDecimal("40000.01000000"), tick.getAsk());

        mockServer.verify();
    }
}
