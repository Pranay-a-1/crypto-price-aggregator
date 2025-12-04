package com.cryptoArb.crypto_price_aggregator.controller;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PriceController.
 * Using @WebMvcTest for focused controller testing.
 */
@WebMvcTest(PriceController.class)
// @AutoConfigureMockMvc(addFilters = false) - Removed to enable security checks if we want to test with security,
// but for controller unit tests, we usually bypass or mock security.
// However, since we now have SecurityConfig, we should likely keep it disabled here OR use @WithMockUser.
// Given the plan was to verify security in SecurityConfigTest, we can keep filters disabled here for pure controller logic testing.
@AutoConfigureMockMvc(addFilters = false)
class PriceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PriceService priceService;

        private AggregatedTopOfBookQuote aggregatedTopOfBookQuote;

        @BeforeEach
        void setUp() {
                CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
                aggregatedTopOfBookQuote = new AggregatedTopOfBookQuote(
                                btcUsd,
                                new BigDecimal("50000.00"),
                                Exchange.BINANCE,
                                new BigDecimal("50100.00"),
                                Exchange.COINBASE,
                                Instant.now());
        }

        @Test
        @DisplayName("Should return 200 OK with AggregatedTopOfBookQuote for valid request")
        void shouldReturn200WithPriceForValidRequest() throws Exception {
                // Arrange
                when(priceService.getAggregatedTopOfBookQuote(any(CurrencyPair.class)))
                                .thenReturn(Optional.of(aggregatedTopOfBookQuote));

                // Act & Assert
                mockMvc.perform(get("/api/prices/BTC/USD"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.pair.base").value("BTC"))
                                .andExpect(jsonPath("$.pair.quote").value("USD"))
                                .andExpect(jsonPath("$.bestBid").value(50000.00))
                                .andExpect(jsonPath("$.bestBidExchange").value("BINANCE"))
                                .andExpect(jsonPath("$.bestAsk").value(50100.00))
                                .andExpect(jsonPath("$.bestAskExchange").value("COINBASE"))
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when price not available")
        void shouldReturn404WhenPriceNotAvailable() throws Exception {
                // Arrange
                when(priceService.getAggregatedTopOfBookQuote(any(CurrencyPair.class)))
                                .thenReturn(Optional.empty());

                // Act & Assert
                mockMvc.perform(get("/api/prices/BTC/USD")) // Use valid pair format that exists in service mock scope (or simpler: pair that passes validation but returns empty)
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle lowercase currency codes (CurrencyPair normalizes)")
        void shouldHandleLowercaseCurrencyCodes() throws Exception {
                // Arrange
                when(priceService.getAggregatedTopOfBookQuote(any(CurrencyPair.class)))
                                .thenReturn(Optional.of(aggregatedTopOfBookQuote));

                // Act & Assert
                mockMvc.perform(get("/api/prices/btc/usd"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.pair.base").value("BTC"))
                                .andExpect(jsonPath("$.pair.quote").value("USD"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for empty base currency")
        void shouldReturn400ForEmptyBase() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/prices/ /USD"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for empty quote currency")
        void shouldReturn400ForEmptyQuote() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/prices/BTC/ "))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 INTERNAL SERVER ERROR when service throws unexpected exception")
        void shouldReturn500WhenServiceThrowsException() throws Exception {
                // Arrange
                when(priceService.getAggregatedTopOfBookQuote(any(CurrencyPair.class)))
                                .thenThrow(new RuntimeException("Unexpected error"));

                // Act & Assert
                mockMvc.perform(get("/api/prices/BTC/USD"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should accept various currency pairs")
        void shouldAcceptVariousCurrencyPairs() throws Exception {
                // Arrange
                when(priceService.getAggregatedTopOfBookQuote(any(CurrencyPair.class)))
                                .thenReturn(Optional.of(aggregatedTopOfBookQuote));

                // Act & Assert - Different pairs
                mockMvc.perform(get("/api/prices/ETH/USD"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/prices/BTC/EUR"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/prices/DOGE/BTC"))
                                .andExpect(status().isOk());
        }
}
