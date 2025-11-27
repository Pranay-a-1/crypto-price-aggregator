package com.cryptoArb.application.controller;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.service.PriceStreamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cryptoArb.config.RequestLoggingFilter;
import com.cryptoArb.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(PriceStreamController.class)
@Import({ SecurityConfig.class, RequestLoggingFilter.class })
class PriceStreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceStreamService priceStreamService;

    @Test
    void streamPrices_ReturnsSseStream() throws Exception {
        PriceTick tick = new PriceTick(
                new CurrencyPair("BTC", "USD"),
                new Exchange("coinbase"),
                Instant.now(),
                new BigDecimal("50000"),
                new BigDecimal("50001"));

        when(priceStreamService.getStream("BTC-USD")).thenReturn(Flux.just(tick));

        mockMvc.perform(get("/api/v1/stream/prices/BTC-USD")
                .with(jwt())
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
        // Verifying body of SSE stream with MockMvc is harder, checking status and
        // content type is good enough for now.
    }
}
