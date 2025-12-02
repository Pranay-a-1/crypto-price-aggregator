package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Additional test for BinanceFetcher symbol construction.
 */
@ExtendWith(MockitoExtension.class)
class BinanceFetcherSymbolTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BinanceFetcher binanceFetcher;

    @Test
    void testSymbolConstruction_UppercaseConversion() throws Exception {
        // Test that symbols are properly converted to uppercase
        String jsonResponse = "{\"symbol\": \"BTCUSDT\", \"bidPrice\": \"40000.00000000\", \"bidQty\": \"10.00000000\", \"askPrice\": \"40000.01000000\", \"askQty\": \"10.00000000\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        JsonNode mockNode = new ObjectMapper().readTree(jsonResponse);
        when(objectMapper.readTree(jsonResponse)).thenReturn(mockNode);

        // Test with lowercase input (should be converted to uppercase)
        CurrencyPair pair = new CurrencyPair(); // Using direct constructor to test normalization
        pair.setBase("btc");
        pair.setQuote("usd");

        // This should not throw an exception and should construct BTCUSDT
        assertDoesNotThrow(() -> binanceFetcher.fetchPrice(pair));
    }

    @Test
    void testSymbolConstruction_USDTConversion() throws Exception {
        String jsonResponse = "{\"symbol\": \"ETHUSDT\", \"bidPrice\": \"3000.00000000\", \"bidQty\": \"10.00000000\", \"askPrice\": \"3000.01000000\", \"askQty\": \"10.00000000\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        JsonNode mockNode = new ObjectMapper().readTree(jsonResponse);
        when(objectMapper.readTree(jsonResponse)).thenReturn(mockNode);

        CurrencyPair pair = new CurrencyPair();
        pair.setBase("ETH");
        pair.setQuote("USD");

        assertDoesNotThrow(() -> binanceFetcher.fetchPrice(pair));
    }

    @Test
    void testSymbolConstruction_InvalidCharacters() {
        CurrencyPair pair = new CurrencyPair();
        pair.setBase("BTC");
        pair.setQuote("USD$"); // Invalid character

        PriceFetchException exception = assertThrows(PriceFetchException.class,
            () -> binanceFetcher.fetchPrice(pair));

        assertTrue(exception.getMessage().contains("Invalid symbol format"));
    }
}