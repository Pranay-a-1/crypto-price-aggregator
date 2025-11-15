package com.cryptoArb.domain_records;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

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
@Entity
@Table(name = "price_tick")
public record PriceTick(
        @EmbeddedId // or use @Id with @GeneratedValue if needed
        CurrencyPair pair,
        Exchange exchange,
        Instant timestamp,
        BigDecimal bidPrice,
        BigDecimal askPrice
) {
}
