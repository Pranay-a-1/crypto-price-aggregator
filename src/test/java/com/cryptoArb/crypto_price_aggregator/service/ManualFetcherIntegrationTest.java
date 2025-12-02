package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for verifying real fetchers in Phase 4.
 * This test makes REAL network calls.
 */
class ManualFetcherIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ManualFetcherIntegrationTest.class);

    @Test
    void testBinanceFetcher() throws Exception {
        BinanceFetcher fetcher = new BinanceFetcher(new org.springframework.web.client.RestTemplate(), new com.fasterxml.jackson.databind.ObjectMapper());
        CurrencyPair pair = CurrencyPair.of("BTC", "USD"); // Maps to BTCUSDT

        try {
            PriceTick tick = fetcher.fetchPrice(pair);
            log.info("Binance Tick: {}", tick);

            assertNotNull(tick);
            assertEquals(Exchange.BINANCE, tick.getExchange());
            assertTrue(tick.getBid().doubleValue() > 0);
            assertTrue(tick.getAsk().doubleValue() > 0);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("451")) {
                log.warn("Binance fetch failed with 451 (Restricted Region). Ignoring for test purposes.");
            } else {
                log.warn("Binance fetch failed: {}", e.getMessage());
                throw e;
            }
        }
    }

    @Test
    void testCoinbaseFetcher() throws Exception {
        CoinbaseFetcher fetcher = new CoinbaseFetcher(new org.springframework.web.client.RestTemplate(), new com.fasterxml.jackson.databind.ObjectMapper());
        CurrencyPair pair = CurrencyPair.of("BTC", "USD"); // Maps to BTC-USD

        try {
            PriceTick tick = fetcher.fetchPrice(pair);
            log.info("Coinbase Tick: {}", tick);

            assertNotNull(tick);
            assertEquals(Exchange.COINBASE, tick.getExchange());
            assertTrue(tick.getBid().doubleValue() > 0);
            assertTrue(tick.getAsk().doubleValue() > 0);
        } catch (Exception e) {
            log.warn("Coinbase fetch failed (might be network issue/rate limit): {}", e.getMessage());
            throw e;
        }
    }
}
