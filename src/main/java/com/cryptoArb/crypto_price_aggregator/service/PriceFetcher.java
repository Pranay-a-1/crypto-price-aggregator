package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.exception.PriceFetchException;

/**
 * Interface for fetching prices from exchanges.
 * <p>
 * Following SOLID principles:
 * - Interface Segregation: Single focused method
 * - Dependency Inversion: Depend on abstraction, not implementation
 * - Open/Closed: New fetchers can be added without modifying existing code
 */
public interface PriceFetcher {

    /**
     * Fetches the current price for a given currency pair from this exchange.
     *
     * @param pair The currency pair to fetch (e.g., BTC/USD)
     * @return A PriceTick containing bestBid, bestAsk, and timestamp
     * @throws PriceFetchException if fetching fails
     */
    PriceTick fetchPrice(CurrencyPair pair) throws PriceFetchException;

    /**
     * Gets the exchange this fetcher is associated with.
     *
     * @return The Exchange enum value
     */
    com.cryptoArb.crypto_price_aggregator.domain.Exchange getExchange();
}
