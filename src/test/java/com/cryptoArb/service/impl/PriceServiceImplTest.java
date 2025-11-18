package com.cryptoArb.service.impl;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.repository.PriceTickRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceImplTest {

    @Mock
    private PriceTickRepository priceTickRepository;

    // @InjectMocks will inject the mock dependencies into the PriceServiceImpl
    //@InjectMocks is different than @Mock as @InjectMocks will inject the mock dependencies into the PriceServiceImpl
    //Whereas if we use @Mock for PriceServiceImpl then it will create a mock object of PriceServiceImpl
    @InjectMocks
    private PriceServiceImpl priceService;

    @Test
    @DisplayName("Should fetch ticks from repo and calculate consolidated price")
    void shouldFetchAndAggregatePrice() {
        // --- Given ---
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        Instant now = Instant.now();

        // Tick 1: Kraken - Bid 50000, Ask 50010
        PriceTick tick1 = new PriceTick(
                pair,
                new Exchange("kraken"),
                now,
                new BigDecimal("50000"),
                new BigDecimal("50010")
        );

        // Tick 2: Coinbase - Bid 49990, Ask 50005
        // (Coinbase has a lower Ask, Kraken has a higher Bid)
        PriceTick tick2 = new PriceTick(
                pair,
                new Exchange("coinbase"),
                now,
                new BigDecimal("49990"),
                new BigDecimal("50005")
        );

        // Mock the repository behavior
        // We assume we will add this method to the repository interface next
        when(priceTickRepository.findByPairBaseAndPairQuote("BTC", "USD"))
                .thenReturn(List.of(tick1, tick2));

        // --- When ---
        // getConsolidatedPriceForPair is the method we are testing
        // it gets the consolidated price for a given currency pair , which contains the best bid and ask
        Optional<ConsolidatedPrice> result = priceService.getConsolidatedPriceForPair(pair);

        // --- Then ---
        assertTrue(result.isPresent(), "Service should return a price");
        ConsolidatedPrice price = result.get();

        // Best Bid should be 50000 (from Kraken)
        assertEquals(new BigDecimal("50000"), price.bestBid());
        assertEquals("kraken", price.bestBidExchange().getId());

        // Best Ask should be 50005 (from Coinbase)
        assertEquals(new BigDecimal("50005"), price.bestAsk());
        assertEquals("coinbase", price.bestAskExchange().getId());
    }

    @Test
    @DisplayName("Should return empty optional if no ticks found")
    void shouldReturnEmptyIfNoData() {
        // --- Given ---
        when(priceTickRepository.findByPairBaseAndPairQuote("ETH", "USD"))
                .thenReturn(Collections.emptyList());

        // --- When ---
        Optional<ConsolidatedPrice> result = priceService.getConsolidatedPriceForPair(
                new CurrencyPair("ETH", "USD")
        );

        // --- Then ---
        assertTrue(result.isEmpty());
    }
}