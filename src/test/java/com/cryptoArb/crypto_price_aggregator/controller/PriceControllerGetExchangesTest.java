package com.cryptoArb.crypto_price_aggregator.controller;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class PriceControllerGetExchangesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceService priceService;

    @Test
    @DisplayName("GET /api/prices/{base}/{quote}/exchanges - Success")
    void getExchangePricesSuccess() throws Exception {
        // Arrange
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Map<String, PriceTick> mockResponse = new HashMap<>();

        mockResponse.put("BINANCE", new PriceTick(pair, Exchange.BINANCE, new BigDecimal("50000.00"), new BigDecimal("50100.00"), Instant.now()));
        mockResponse.put("COINBASE", new PriceTick(pair, Exchange.COINBASE, new BigDecimal("50050.00"), new BigDecimal("50150.00"), Instant.now()));

        when(priceService.getLatestPriceTicks(any(CurrencyPair.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/prices/BTC/USD/exchanges")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BINANCE.bid").value(50000.00))
                .andExpect(jsonPath("$.COINBASE.bid").value(50050.00));
    }

    @Test
    @DisplayName("GET /api/prices/{base}/{quote}/exchanges - Invalid Input")
    void getExchangePricesInvalidInput() throws Exception {
        mockMvc.perform(get("/api/prices/ /USD/exchanges")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
