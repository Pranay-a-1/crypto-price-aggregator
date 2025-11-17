package com.cryptoArb.javaImpl.domain_records;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a potential arbitrage opportunity found by comparing prices.
 * This is a final class, converted from a record, to implement the Builder Pattern.
 *
 * @param pair             The currency pair
 * @param timestamp        The time the opportunity was found (from the latest tick)
 * @param buyExchange      The exchange to buy from (lowest ask)
 * @param buyPrice         The price to buy at (lowest ask)
 * @param sellExchange     The exchange to sell at (highest bid)
 * @param sellPrice        The price to sell at (highest bid)
 *
 */
public final class ArbitrageOpportunity {

    // 1. Fields are now private and final
    private final CurrencyPair pair;
    private final Instant timestamp;
    private final Exchange buyExchange;
    private final BigDecimal buyPrice;
    private final Exchange sellExchange;
    private final BigDecimal sellPrice;

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    // 2. The constructor is now PRIVATE.
    // This is the key change that will break the old test (as intended).
    private ArbitrageOpportunity(
            CurrencyPair pair,
            Instant timestamp,
            Exchange buyExchange,
            BigDecimal buyPrice,
            Exchange sellExchange,
            BigDecimal sellPrice
    ) {
        // Validation logic can be placed here
        this.pair = Objects.requireNonNull(pair, "Pair cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.buyExchange = Objects.requireNonNull(buyExchange, "Buy exchange cannot be null");
        this.buyPrice = Objects.requireNonNull(buyPrice, "Buy price cannot be null");
        this.sellExchange = Objects.requireNonNull(sellExchange, "Sell exchange cannot be null");
        this.sellPrice = Objects.requireNonNull(sellPrice, "Sell price cannot be null");
    }

    // 3. Public "getter" methods (to replace the record's accessors)
    public CurrencyPair pair() { return pair; }
    public Instant timestamp() { return timestamp; }
    public Exchange buyExchange() { return buyExchange; }
    public BigDecimal buyPrice() { return buyPrice; }
    public Exchange sellExchange() { return sellExchange; }
    public BigDecimal sellPrice() { return sellPrice; }

    /**
     * Calculates the profit percentage for this opportunity.
     * Formula: (sellPrice - buyPrice) / buyPrice
     */
    public BigDecimal profitPercentage() {
        BigDecimal profit = sellPrice.subtract(buyPrice);
        return profit.divide(buyPrice, MC);
    }

    // 4. The public static inner Builder class
    public static class Builder {
        // Builder fields
        private CurrencyPair pair;
        private Instant timestamp;
        private Exchange buyExchange;
        private BigDecimal buyPrice;
        private Exchange sellExchange;
        private BigDecimal sellPrice;

        // "Setter" methods that return 'this' for chaining
        public Builder pair(CurrencyPair pair) {
            this.pair = pair;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder buyExchange(Exchange buyExchange) {
            this.buyExchange = buyExchange;
            return this;
        }

        public Builder buyPrice(BigDecimal buyPrice) {
            this.buyPrice = buyPrice;
            return this;
        }

        public Builder sellExchange(Exchange sellExchange) {
            this.sellExchange = sellExchange;
            return this;
        }

        public Builder sellPrice(BigDecimal sellPrice) {
            this.sellPrice = sellPrice;
            return this;
        }

        // The build() method calls the private constructor
        public ArbitrageOpportunity build() {
            return new ArbitrageOpportunity(
                    pair, timestamp, buyExchange, buyPrice, sellExchange, sellPrice
            );
        }
    }

    // 5. We must add equals(), hashCode(), and toString()
    //    to make our class behave just like the record did in tests.
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ArbitrageOpportunity) obj;
        return Objects.equals(this.pair, that.pair) &&
                Objects.equals(this.timestamp, that.timestamp) &&
                Objects.equals(this.buyExchange, that.buyExchange) &&
                Objects.equals(this.buyPrice, that.buyPrice) &&
                Objects.equals(this.sellExchange, that.sellExchange) &&
                Objects.equals(this.sellPrice, that.sellPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, timestamp, buyExchange, buyPrice, sellExchange, sellPrice);
    }

    @Override
    public String toString() {
        return "ArbitrageOpportunity[" +
                "pair=" + pair + ", " +
                "timestamp=" + timestamp + ", " +
                "buyExchange=" + buyExchange + ", " +
                "buyPrice=" + buyPrice + ", " +
                "sellExchange=" + sellExchange + ", " +
                "sellPrice=" + sellPrice + ']';
    }
}