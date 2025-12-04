package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.*;
import com.cryptoArb.crypto_price_aggregator.repository.ArbitrageRepository;
import com.cryptoArb.crypto_price_aggregator.service.impl.ArbitrageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Unit tests for ArbitrageService.
 * 
 * Following TDD: Write tests first (RED), implement service (GREEN), refactor.
 * Uses Mockito to mock dependencies (PriceService and ArbitrageRepository).
 */
@ExtendWith(MockitoExtension.class)
class ArbitrageServiceTest {

    @Mock
    private PriceService priceService;

    @Mock
    private ArbitrageRepository arbitrageRepository;

    private ArbitrageService arbitrageService;

    @BeforeEach
    void setUp() {
        arbitrageService = new ArbitrageServiceImpl(priceService, arbitrageRepository);
    }

    @Test
    void shouldDetectArbitrageWhenBestBidExceedsBestAsk() {
        // Given - Price ticks showing arbitrage opportunity
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        Map<String, PriceTick> ticks = new HashMap<>();
        // Binance: Lower ask price (good for buying)
        ticks.put("BINANCE", createTick(pair, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50100")));
        // Coinbase: Higher bid price (good for selling)
        ticks.put("COINBASE", createTick(pair, Exchange.COINBASE,
                new BigDecimal("50200"), new BigDecimal("50300")));

        when(priceService.getLatestPriceTicks(pair)).thenReturn(ticks);
        when(arbitrageRepository.save(any(ArbitrageOpportunity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<ArbitrageOpportunity> result = arbitrageService.detectArbitrage(pair);

        // Then
        assertTrue(result.isPresent());
        ArbitrageOpportunity opportunity = result.get();

        // Should buy at Binance (lower ask) and sell at Coinbase (higher bid)
        assertEquals(Exchange.BINANCE, opportunity.getBuyExchange());
        assertEquals(Exchange.COINBASE, opportunity.getSellExchange());
        assertEquals(new BigDecimal("50100"), opportunity.getBuyPrice()); // Binance ask
        assertEquals(new BigDecimal("50200"), opportunity.getSellPrice()); // Coinbase bid

        // Verify profit percentage calculation
        assertTrue(opportunity.getProfitPercentage().compareTo(BigDecimal.ZERO) > 0);

        // Verify repository save was called
        verify(arbitrageRepository, times(1)).save(any(ArbitrageOpportunity.class));
    }

    @Test
    void shouldNotDetectArbitrageWhenNoOpportunityExists() {
        // Given - Normal market conditions (no arbitrage)
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        Map<String, PriceTick> ticks = new HashMap<>();
        // Both exchanges have similar prices, no arbitrage
        ticks.put("BINANCE", createTick(pair, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50100")));
        ticks.put("COINBASE", createTick(pair, Exchange.COINBASE,
                new BigDecimal("50000"), new BigDecimal("50100")));

        when(priceService.getLatestPriceTicks(pair)).thenReturn(ticks);

        // When
        Optional<ArbitrageOpportunity> result = arbitrageService.detectArbitrage(pair);

        // Then
        assertFalse(result.isPresent());

        // Verify repository save was NOT called (no arbitrage to save)
        verify(arbitrageRepository, never()).save(any(ArbitrageOpportunity.class));
    }

    @Test
    void shouldCalculateCorrectProfitPercentage() {
        // Given - Specific prices to verify calculation
        CurrencyPair pair = new CurrencyPair("ETH", "USD");

        Map<String, PriceTick> ticks = new HashMap<>();
        // Buy at 3000, sell at 3030 = 1% profit
        ticks.put("BINANCE", createTick(pair, Exchange.BINANCE,
                new BigDecimal("2990"), new BigDecimal("3000")));
        ticks.put("KRAKEN", createTick(pair, Exchange.KRAKEN,
                new BigDecimal("3030"), new BigDecimal("3040")));

        when(priceService.getLatestPriceTicks(pair)).thenReturn(ticks);
        when(arbitrageRepository.save(any(ArbitrageOpportunity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<ArbitrageOpportunity> result = arbitrageService.detectArbitrage(pair);

        // Then
        assertTrue(result.isPresent());
        ArbitrageOpportunity opportunity = result.get();

        // Profit % = ((3030 - 3000) / 3000) * 100 = 1.0%
        BigDecimal expectedProfit = new BigDecimal("1.0000");
        assertEquals(0, expectedProfit.compareTo(opportunity.getProfitPercentage()));
    }

    @Test
    void shouldReturnEmptyWhenNoTicksAvailable() {
        // Given - No price ticks available
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        when(priceService.getLatestPriceTicks(pair)).thenReturn(new HashMap<>());

        // When
        Optional<ArbitrageOpportunity> result = arbitrageService.detectArbitrage(pair);

        // Then
        assertFalse(result.isPresent());
        verify(arbitrageRepository, never()).save(any(ArbitrageOpportunity.class));
    }

    @Test
    void shouldHandleSingleExchangeGracefully() {
        // Given - Only one exchange has data (no arbitrage possible)
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        Map<String, PriceTick> ticks = new HashMap<>();
        ticks.put("BINANCE", createTick(pair, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50100")));

        when(priceService.getLatestPriceTicks(pair)).thenReturn(ticks);

        // When
        Optional<ArbitrageOpportunity> result = arbitrageService.detectArbitrage(pair);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldGetRecentOpportunitiesWithLimit() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        int limit = 5;

        List<ArbitrageOpportunity> mockOpportunities = List.of(
                createOpportunity(pair, Instant.now()),
                createOpportunity(pair, Instant.now().minusSeconds(60)));

        when(arbitrageRepository.findByCurrencyPair_BaseAndCurrencyPair_QuoteOrderByDetectedAtDesc(
                "BTC", "USD")).thenReturn(mockOpportunities);

        // When
        List<ArbitrageOpportunity> result = arbitrageService.getRecentOpportunities(pair, limit);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= limit);
        assertEquals(2, result.size());
        verify(arbitrageRepository).findByCurrencyPair_BaseAndCurrencyPair_QuoteOrderByDetectedAtDesc(
                "BTC", "USD");
    }

    @Test
    void shouldPersistDetectedArbitrageOpportunity() {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        Map<String, PriceTick> ticks = new HashMap<>();
        ticks.put("BINANCE", createTick(pair, Exchange.BINANCE,
                new BigDecimal("50000"), new BigDecimal("50100")));
        ticks.put("COINBASE", createTick(pair, Exchange.COINBASE,
                new BigDecimal("50200"), new BigDecimal("50300")));

        when(priceService.getLatestPriceTicks(pair)).thenReturn(ticks);
        when(arbitrageRepository.save(any(ArbitrageOpportunity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        arbitrageService.detectArbitrage(pair);

        // Then - Verify the opportunity was saved with correct values
        ArgumentCaptor<ArbitrageOpportunity> captor = ArgumentCaptor.forClass(ArbitrageOpportunity.class);
        verify(arbitrageRepository).save(captor.capture());

        ArbitrageOpportunity saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(pair, saved.getCurrencyPair());
        assertNotNull(saved.getDetectedAt());
    }

    // Helper methods
    private PriceTick createTick(CurrencyPair pair, Exchange exchange,
            BigDecimal bid, BigDecimal ask) {
        return new PriceTick(pair, exchange, bid, ask, Instant.now());
    }

    private ArbitrageOpportunity createOpportunity(CurrencyPair pair, Instant detectedAt) {
        return new ArbitrageOpportunity(
                pair,
                Exchange.BINANCE,
                Exchange.COINBASE,
                new BigDecimal("50000"),
                new BigDecimal("50200"),
                new BigDecimal("0.40"),
                detectedAt);
    }
}
