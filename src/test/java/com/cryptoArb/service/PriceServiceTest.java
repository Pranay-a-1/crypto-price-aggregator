package com.cryptoArb.service;

import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceServiceTest {


    // Our mock data
    private Exchange coinbase = new Exchange("coinbase");
    private Exchange kraken = new Exchange("kraken");
    private CurrencyPair btcUsd = new CurrencyPair("BTC", "USD");

    private List<PriceTick> allTicks;

    @BeforeEach
    void setUp() {
        // Create a list of mixed ticks before each test
        allTicks = List.of(
                new PriceTick(btcUsd, coinbase, 1000L, 50000, 50001),
                new PriceTick(btcUsd, kraken, 1001L, 49999, 50000),
                new PriceTick(btcUsd, coinbase, 1002L, 50002, 50003)
        );
    }

    @Test
    @DisplayName("Should filter a list of ticks and return only those from Coinbase")
    void givenTicksFromMultipleExchanges_whenFilterByCoinbase_thenReturnsOnlyCoinbaseTicks() {
        // Given: A PriceService and a list of ticks (from setUp)
        // These two lines will NOT compile
        PriceService priceService = new PriceService();
        String targetExchangeId = "coinbase";

        // When: We call the hard-coded filter method
        List<PriceTick> coinbaseTicks = priceService.filterCoinbaseTicks(allTicks);

        // Then: The resulting list should only have the 2 coinbase ticks
        assertEquals(2, coinbaseTicks.size(), "Should only be 2 Coinbase ticks");

        // And we can double-check that they are ALL from coinbase
        for (PriceTick tick : coinbaseTicks) {
            assertEquals(targetExchangeId, tick.exchange().id(), "Tick should be from Coinbase");
        }
    }




    @Test
    @DisplayName("Should filter ticks using a flexible Predicate (anonymous class)")
    void givenTicks_whenFilterByPredicate_thenReturnsFilteredTicks() {
        // Given: A PriceService (this time, it needs to be instantiated in the test)
        PriceService priceService = new PriceService();

        // And a Predicate, written as an anonymous class, to find "kraken" ticks
        //BEFORE:
        // Predicate<PriceTick> krakenPredicate = new Predicate<PriceTick>() {
        //     @Override
        //     public boolean test(PriceTick tick) {
        //         return "kraken".equals(tick.exchange().id());
        //     }
        // };

        // AFTER (Refactored to a Lambda):
        Predicate<PriceTick> krakenPredicate = tick -> "kraken".equals(tick.exchange().id());

        // When: We call our NEW, non-existent filter method
        // This line will NOT compile
        List<PriceTick> krakenTicks = priceService.filter(allTicks, krakenPredicate);

        // Then: The result should be correct
        assertEquals(1, krakenTicks.size(), "Should find the 1 kraken tick");
        assertEquals("kraken", krakenTicks.get(0).exchange().id());
    }
}