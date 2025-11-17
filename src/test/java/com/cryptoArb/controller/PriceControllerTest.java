package com.cryptoArb.controller;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SLICE TEST for the PriceController.
 */
// --- MODIFICATION ---
// We explicitly EXCLUDE the security configurations from this slice test.
// This allows us to test the controller's logic in isolation.
@WebMvcTest(controllers = PriceController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        })
// --- END MODIFICATION ---
@DisplayName("PriceController Web Slice Test")
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceService mockPriceService;

    @MockitoBean
    private ArbitrageService mockArbitrageService;

    @Test
    @DisplayName("GET /api/v1/price/BTC-USD should return 200 OK with price data")
    void givenPairExists_whenGetPriceByPair_thenReturns200Ok() throws Exception {
        // --- Given (Arrange) ---
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        ConsolidatedPrice mockPrice = new ConsolidatedPrice(
                pair,
                Instant.now(),
                new BigDecimal("50000"),
                new Exchange("kraken"),
                new BigDecimal("50001"),
                new Exchange("coinbase")
        );

        // when PriceService.getConsolidatedPriceForPair is called, return the mock price
        when(mockPriceService.getConsolidatedPriceForPair(any(CurrencyPair.class)))
                .thenReturn(Optional.of(mockPrice));

        // --- When (Act) & Then (Assert) ---
        // perform a GET request to /api/v1/price/BTC-USD
        // accept JSON response
        mockMvc.perform(get("/api/v1/price/BTC-USD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // This should now pass
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pair.base").value("BTC"))
                .andExpect(jsonPath("$.bestBid").value("50000"));
    }

    @Test
    @DisplayName("GET /api/v1/price/UNKNOWN-PAIR should return 404 Not Found")
    void givenPairDoesNotExist_whenGetPriceByPair_thenReturns404NotFound() throws Exception {
        // --- Given (Arrange) ---
        when(mockPriceService.getConsolidatedPriceForPair(any(CurrencyPair.class)))
                .thenReturn(Optional.empty());

        // --- When (Act) & Then (Assert) ---
        mockMvc.perform(get("/api/v1/price/UNKNOWN-USD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // This should now pass
    }


    // === NEW TEST ===
    @Test
    @DisplayName("GET /api/v1/arbitrage should return 200 OK with list")
    void givenOpportunitiesExist_whenGetArbitrage_thenReturns200Ok() throws Exception {
        // --- Given (Arrange) ---
        // We will test the "happy path" where the service returns an empty list.
        when(mockArbitrageService.getRecentOpportunities()).thenReturn(List.of());

        // --- When (Act) & Then (Assert) ---
        // perform a GET request to /api/v1/arbitrage
        // accept JSON response
        // mockMVC.perform does the actual HTTP request
        // and returns a ResultActions object
        // which we can chain matchers on like this:
        // expect status to be 200 OK
        // expect content type to be JSON
        // expect JSON array to be empty
        mockMvc.perform(get("/api/v1/arbitrage")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty()); // Check for an empty JSON array: []
    }
}