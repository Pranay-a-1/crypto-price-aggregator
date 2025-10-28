package com.cryptoArb.domain;

import java.math.BigDecimal;

/**
 * Represents a potential arbitrage opportunity found by comparing prices.
 *
 * @param pair             The currency pair
 * @param timestamp        The epoch millisecond timestamp of this finding
 * @param buyExchange      The exchange to buy from (lowest ask)
 * @param buyPrice         The price to buy at (lowest ask)
 * @param sellExchange     The exchange to sell at (highest bid)
 * @param sellPrice        The price to sell at (highest bid)
 * @param profitPercentage The calculated profit percentage
 */
public record ArbitrageOpportunity(
        CurrencyPair pair,
        long timestamp,
        String buyExchange,
        BigDecimal buyPrice,
        String sellExchange,
        BigDecimal sellPrice,
        BigDecimal profitPercentage
) {
}