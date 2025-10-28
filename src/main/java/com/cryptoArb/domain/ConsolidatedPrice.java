package com.cryptoArb.domain;

import java.math.BigDecimal;

/**
 * Represents the best bid/ask price for a pair, aggregated from all exchanges.
 *
 * @param pair            The currency pair
 * @param timestamp       The epoch millisecond timestamp of this consolidation
 * @param bestBid         The highest bid price found across all exchanges
 * @param bestBidExchange The exchange offering the best bid
 * @param bestAsk         The lowest ask price found across all exchanges
 * @param bestAskExchange The exchange offering the best ask
 */
public record ConsolidatedPrice(
        CurrencyPair pair,
        long timestamp,
        BigDecimal bestBid,
        String bestBidExchange,
        BigDecimal bestAsk,
        String bestAskExchange
) {
}