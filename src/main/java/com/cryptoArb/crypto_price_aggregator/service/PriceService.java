package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;

import java.util.Map;
import java.util.Optional;

public interface PriceService {
    /**
     * Retrieves the latest AggregatedTopOfBookQuote for a given currency pair.
     *
     * @param pair The currency pair to fetch (e.g., BTC/USD).
     * @return An Optional containing the quote if found, or empty if not available.
     */
    Optional<AggregatedTopOfBookQuote> getAggregatedTopOfBookQuote(CurrencyPair pair);


    /**
     * Retrieves the latest price ticks from all available exchanges for a given currency pair.
     *
     * @param pair The currency pair to fetch (e.g., BTC/USD).
     * @return A map where key is the exchange name and value is the PriceTick.
     */
    Map<String, PriceTick> getLatestPriceTicks(CurrencyPair pair);

}