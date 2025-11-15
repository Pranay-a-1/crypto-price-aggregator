package com.cryptoArb.domain_POJOs;

import com.cryptoArb.domain_records.CurrencyPair;
import com.cryptoArb.domain_records.Exchange;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Represents a potential arbitrage opportunity found by comparing prices.
 *
 * @param pair             The currency pair
 * @param timestamp        The time the opportunity was found (from the latest tick)
 * @param buyExchange      The exchange to buy from (lowest ask)
 * @param buyPrice         The price to buy at (lowest ask)
 * @param sellExchange     The exchange to sell at (highest bid)
 * @param sellPrice        The price to sell at (highest bid)
 */
public record ArbitrageOpportunity_POJO(
        CurrencyPair pair,
        Instant timestamp,
        Exchange buyExchange,
        BigDecimal buyPrice,
        Exchange sellExchange,
        BigDecimal sellPrice
) {

    // 5. Define a MathContext for precise, non-terminating division
    // meaning we want to avoid issues when the division results in a repeating decimal
    // for example 1 / 3 = 0.3333... , so we set a precision and rounding mode
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Calculates the profit percentage for this opportunity.
     * Formula: (sellPrice - buyPrice) / buyPrice
     *
     * @return The profit as a percentage (e.g., 0.01 for 1%).
     */
    public BigDecimal profitPercentage() {
        // (sell - buy)
        BigDecimal profit = sellPrice.subtract(buyPrice);

        // profit / buy
        // We use the MathContext to handle division with many decimal places
        return profit.divide(buyPrice, MC);
    }
}