package com.cryptoArb.domain;

import java.math.BigDecimal;

/**
 * Represents a price snapshot from a specific exchange.
 * Converted to a record (Phase 3 task) for immutability and conciseness.
 *
 * @param pair       The currency pair (e.g., BTC/USD)
 * @param exchange   The exchange (e.g., coinbase)
 * @param timestamp  The epoch millisecond timestamp of the tick
 * @param bidPrice   The highest price a buyer is willing to pay
 * @param askPrice   The lowest price a seller is willing to accept
 */
public record PriceTick(
        CurrencyPair pair,
        Exchange exchange,
        long timestamp,
        BigDecimal bidPrice, // Changed from double
        BigDecimal askPrice  // Changed from double
) {
}
