package com.cryptoArb.crypto_price_aggregator.controller;

import com.cryptoArb.crypto_price_aggregator.domain.ArbitrageOpportunity;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.service.ArbitrageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArbitrageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArbitrageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArbitrageService arbitrageService;

    @Test
    @DisplayName("Should return list of arbitrage opportunities")
    void shouldReturnOpportunities() throws Exception {
        // Arrange
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                pair,
                Exchange.BINANCE,
                Exchange.COINBASE,
                new BigDecimal("50000.00"),
                new BigDecimal("51000.00"),
                new BigDecimal("2.00"),
                Instant.now()
        );

        when(arbitrageService.getRecentOpportunities(any(CurrencyPair.class), eq(10)))
                .thenReturn(List.of(opportunity));

        // Act & Assert
        mockMvc.perform(get("/api/arbitrage/BTC/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyExchange").value("BINANCE"))
                .andExpect(jsonPath("$[0].sellExchange").value("COINBASE"))
                .andExpect(jsonPath("$[0].profitPercentage").value(2.00));
    }

    @Test
    @DisplayName("Should return empty list when no opportunities found")
    void shouldReturnEmptyList() throws Exception {
        // Arrange
        when(arbitrageService.getRecentOpportunities(any(CurrencyPair.class), eq(10)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/arbitrage/ETH/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should validate currency codes")
    void shouldValidateCurrencyCodes() throws Exception {
        mockMvc.perform(get("/api/arbitrage/ /USD"))
                .andExpect(status().isBadRequest());
    }
}
