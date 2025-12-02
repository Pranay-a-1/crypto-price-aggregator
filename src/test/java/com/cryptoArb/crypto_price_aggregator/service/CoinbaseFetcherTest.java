package com.cryptoArb.crypto_price_aggregator.service;

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
 * Unit test for CoinbaseFetcher using MockRestServiceServer.
 */
@ExtendWith(MockitoExtension.class)
class CoinbaseFetcherTest {

    private CoinbaseFetcher fetcher;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper objectMapper = new ObjectMapper();
        fetcher = new CoinbaseFetcher(restTemplate, objectMapper);
    }

    @Test
    void testFetchPrice_Success() throws Exception {
        String jsonResponse = "{\"trade_id\": 4729088, \"price\": \"333.99\", \"size\": \"0.193\", \"bid\": \"333.98\", \"ask\": \"333.99\", \"volume\": \"5957.11914015\", \"time\": \"2015-11-14T20:46:03.511254Z\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertNotNull(tick);
        assertEquals(Exchange.COINBASE, tick.getExchange());
        assertEquals(new BigDecimal("333.98"), tick.getBid());
        assertEquals(new BigDecimal("333.99"), tick.getAsk());

        mockServer.verify();
    }
}
