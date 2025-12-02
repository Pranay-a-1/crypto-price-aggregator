package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.service.impl.KrakenFetcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Unit test for KrakenFetcher using MockRestServiceServer.
 */
@ExtendWith(MockitoExtension.class)
class KrakenFetcherTest {

    private KrakenFetcher fetcher;
    private MockRestServiceServer mockServer;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper objectMapper = new ObjectMapper();
        fetcher = new KrakenFetcher(restTemplate, objectMapper, eventPublisher);
    }

    @Test
    void testFetchPrice_Success() throws Exception {
        // Sample response based on user input
        String jsonResponse = "{\n" +
                "    \"error\": [],\n" +
                "    \"result\": {\n" +
                "        \"XXBTZUSD\": {\n" +
                "            \"a\": [\n" +
                "                \"87558.90000\",\n" +
                "                \"2\",\n" +
                "                \"2.000\"\n" +
                "            ],\n" +
                "            \"b\": [\n" +
                "                \"87558.80000\",\n" +
                "                \"1\",\n" +
                "                \"1.000\"\n" +
                "            ],\n" +
                "            \"c\": [\n" +
                "                \"87558.90000\",\n" +
                "                \"0.00150000\"\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}";

        mockServer.expect(requestTo("https://api.kraken.com/0/public/Ticker?pair=XBTUSD"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertNotNull(tick);
        assertEquals(Exchange.KRAKEN, tick.getExchange());
        assertEquals(new BigDecimal("87558.80000"), tick.getBid());
        assertEquals(new BigDecimal("87558.90000"), tick.getAsk());

        ArgumentCaptor<PriceTickFetchedEvent> eventCaptor = ArgumentCaptor.forClass(PriceTickFetchedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(tick, eventCaptor.getValue().getPriceTick());


        mockServer.verify();
    }
}
