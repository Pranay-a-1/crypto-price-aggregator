package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;
import com.cryptoArb.domain.ConsolidatedPrice;
import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArbitrageServiceTest {

    @Test
    @DisplayName("Should find one arbitrage opportunity from a map of consolidated prices")
    void givenPriceMap_whenFindArbitrage_thenReturnsOpportunities() {
        // --- Given ---
        // A map of consolidated prices from our PriceService

        Instant now = Instant.now();
        CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");
        CurrencyPair ethUsd = new CurrencyPair("ETH", "USD");

        // 1. A normal price (no arbitrage): Best Bid (50000) < Best Ask (50001)
        ConsolidatedPrice btcPrice = new ConsolidatedPrice(
                btcUsd,
                now,
                new BigDecimal("50000"), // bestBid
                new Exchange("kraken"),                // bestBidExchange
                new BigDecimal("50001"), // bestAsk
                new Exchange("coinbase")               // bestAskExchange
        );

        // 2. An arbitrage opportunity: Best Bid (3000) > Best Ask (2999)
        //    (We can buy at 2999 on coinbase and sell at 3000 on kraken)
        ConsolidatedPrice ethPrice = new ConsolidatedPrice(
                ethUsd,
                now,
                new BigDecimal("3000"), // bestBid
                new Exchange("kraken"),               // bestBidExchange
                new BigDecimal("2999"), // bestAsk
                new Exchange("coinbase")             // bestAskExchange
        );

        Map<CurrencyPair, ConsolidatedPrice> priceMap = Map.of(
                btcUsd, btcPrice,
                ethUsd, ethPrice
        );

        // --- When ---
        // We create our new service and call a method that doesn't exist yet
        ArbitrageService arbitrageService = new ArbitrageService(); // This class doesn't exist

        // This method doesn't exist (RED)
        List<ArbitrageOpportunity> opportunities = arbitrageService.findArbitrageOpportunities(priceMap);

        // --- Then ---
        // We should find exactly one opportunity
        assertNotNull(opportunities);
        assertEquals(1, opportunities.size(), "Should find exactly one opportunity");

        // Verify the details of the opportunity
        ArbitrageOpportunity opportunity = opportunities.get(0);
        assertEquals(ethUsd, opportunity.pair());
        assertEquals(now, opportunity.timestamp());

        // We BUY at the LOWEST ASK
        assertEquals("coinbase", opportunity.buyExchange().id());
        assertEquals(new BigDecimal("2999"), opportunity.buyPrice());

        // We SELL at the HIGHEST BID
        assertEquals("kraken", opportunity.sellExchange().id());
        assertEquals(new BigDecimal("3000"), opportunity.sellPrice());

        // Verify the profit calculation: (sell - buy) / buy
        // (3000 - 2999) / 2999 = 1 / 2999 = 0.0003334...
        BigDecimal expectedProfit = new BigDecimal("0.0003"); // Using 4 decimal places for test
        BigDecimal actualProfit = opportunity.profitPercentage()
                .setScale(4, RoundingMode.HALF_UP); // setscale means rounding for comparison ; half up means round 5 and above up

        assertEquals(expectedProfit, actualProfit, "Profit percentage calculation is incorrect");
    }

    @Test
    @DisplayName("Should return an empty list when no arbitrage is found")
    void givenPriceMapWithNoArb_whenFindArbitrage_thenReturnsEmptyList() {
        // --- Given ---
        Instant now = Instant.now();
        CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");

        // A normal price (no arbitrage)
        ConsolidatedPrice btcPrice = new ConsolidatedPrice(
                btcUsd,
                now,
                new BigDecimal("50000"), // bestBid
                new Exchange("kraken"),                // bestBidExchange
                new BigDecimal("50001"), // bestAsk
                new Exchange("coinbase")               // bestAskExchange
        );

        Map<CurrencyPair, ConsolidatedPrice> priceMap = Map.of(btcUsd, btcPrice);

        // --- When ---
        ArbitrageService arbitrageService = new ArbitrageService(); // This class doesn't exist

        // This method doesn't exist (RED)
        List<ArbitrageOpportunity> opportunities = arbitrageService.findArbitrageOpportunities(priceMap);

        // --- Then ---
        assertNotNull(opportunities);
        assertEquals(0, opportunities.size(), "Should find no opportunities");
    }

}