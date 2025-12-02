package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.event.PriceTickFetchedEvent;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Unit test for CoinbaseFetcher using MockRestServiceServer.
 * Tests various scenarios including success, error handling, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class CoinbaseFetcherTest {

    private CoinbaseFetcher fetcher;
    private MockRestServiceServer mockServer;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper objectMapper = new ObjectMapper();
        fetcher = new CoinbaseFetcher(restTemplate, objectMapper, eventPublisher);
    }

    /**
     * Test successful price fetch for BTC-USD pair
     */
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
        assertEquals(pair, tick.getPair());

        ArgumentCaptor<PriceTickFetchedEvent> eventCaptor = ArgumentCaptor.forClass(PriceTickFetchedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(tick, eventCaptor.getValue().getPriceTick());


        mockServer.verify();
    }

    /**
     * Test successful price fetch for ETH-USD pair
     */
    @Test
    void testFetchPrice_ETH_USD_Success() throws Exception {
        String jsonResponse = "{\"bid\": \"2000.50\", \"ask\": \"2000.75\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/ETH-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("ETH", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertNotNull(tick);
        assertEquals(Exchange.COINBASE, tick.getExchange());
        assertEquals(new BigDecimal("2000.50"), tick.getBid());
        assertEquals(new BigDecimal("2000.75"), tick.getAsk());

        mockServer.verify();
    }

    /**
     * Test successful price fetch for DOGE-USD pair
     */
    @Test
    void testFetchPrice_DOGE_USD_Success() throws Exception {
        String jsonResponse = "{\"bid\": \"0.08\", \"ask\": \"0.09\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/DOGE-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("DOGE", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertNotNull(tick);
        assertEquals(new BigDecimal("0.08"), tick.getBid());
        assertEquals(new BigDecimal("0.09"), tick.getAsk());

        mockServer.verify();
    }

    /**
     * Test API server error (500 Internal Server Error)
     */
    @Test
    void testFetchPrice_ServerError() {
        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withServerError());

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");

        assertThrows(PriceFetchException.class, () -> fetcher.fetchPrice(pair));
        mockServer.verify();
    }

    /**
     * Test handling of invalid JSON response
     */
    @Test
    void testFetchPrice_InvalidJson() {
        String invalidJson = "{\"invalid\": \"response\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(invalidJson, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");

        assertThrows(PriceFetchException.class, () -> fetcher.fetchPrice(pair));
        mockServer.verify();
    }

    /**
     * Test handling of missing bid field in response
     */
    @Test
    void testFetchPrice_MissingBidField() {
        String jsonResponse = "{\"ask\": \"333.99\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");

        assertThrows(PriceFetchException.class, () -> fetcher.fetchPrice(pair));
        mockServer.verify();
    }

    /**
     * Test handling of missing ask field in response
     */
    @Test
    void testFetchPrice_MissingAskField() {
        String jsonResponse = "{\"bid\": \"333.98\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");

        assertThrows(PriceFetchException.class, () -> fetcher.fetchPrice(pair));
        mockServer.verify();
    }

    /**
     * Test that fetcher returns correct exchange
     */
    @Test
    void testGetExchange() {
        assertEquals(Exchange.COINBASE, fetcher.getExchange());
    }

    /**
     * Test with very large price values
     */
    @Test
    void testFetchPrice_LargePriceValues() throws Exception {
        String jsonResponse = "{\"bid\": \"99999.99\", \"ask\": \"100000.00\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertEquals(new BigDecimal("99999.99"), tick.getBid());
        assertEquals(new BigDecimal("100000.00"), tick.getAsk());

        mockServer.verify();
    }

    /**
     * Test with very small price values
     */
    @Test
    void testFetchPrice_SmallPriceValues() throws Exception {
        String jsonResponse = "{\"bid\": \"0.00001\", \"ask\": \"0.00002\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertEquals(new BigDecimal("0.00001"), tick.getBid());
        assertEquals(new BigDecimal("0.00002"), tick.getAsk());

        mockServer.verify();
    }

    /**
     * Test that bid is always less than or equal to ask
     */
    @Test
    void testFetchPrice_BidLessThanAsk() throws Exception {
        String jsonResponse = "{\"bid\": \"100.00\", \"ask\": \"101.00\"}";

        mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
                .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertTrue(tick.getBid().compareTo(tick.getAsk()) <= 0, "Bid should be less than or equal to ask");

        mockServer.verify();
    }
}
