package com.cryptoArb.domain_spring;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A "Heavy" version of ArbitrageOpportunity designed to demonstrate JPA relationships.
 * <p>
 * UNLIKE the original ArbitrageOpportunity (which uses @Embedded for efficiency),
 * this class uses @ManyToOne relationships to the ExchangeInfo entity.
 * <p>
 * This structure allows us to demonstrate the "N+1 Select Problem" when
 * fetching a list of these opportunities.
 */
@Entity
@Table(name = "linked_arbitrage_opportunity")
public class LinkedArbitrageOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We reuse the efficient embeddable for the pair
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "base", column = @Column(name = "base_currency")),
            @AttributeOverride(name = "quote", column = @Column(name = "quote_currency"))
    })
    private CurrencyPair pair;

    @Column(nullable = false)
    private Instant timestamp;

    // --- THE CAUSE OF N+1 START ---

    /**
     * The "Buy" exchange.
     * FETCH TYPE: EAGER (Default for @ManyToOne).
     * This means when you load this opportunity, Hibernate tries to load this exchange immediately.
     */
    @ManyToOne(fetch = FetchType.LAZY) // We use LAZY to demonstrate the classic proxy initialization problem
    @JoinColumn(name = "buy_exchange_id", nullable = false)
    private ExchangeInfo buyExchange;

    /**
     * The "Sell" exchange.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_exchange_id", nullable = false)
    private ExchangeInfo sellExchange;

    // --- THE CAUSE OF N+1 END ---

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal buyPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal sellPrice;

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal profitPercentage;

    protected LinkedArbitrageOpportunity() {}

    public LinkedArbitrageOpportunity(CurrencyPair pair, Instant timestamp,
                                      ExchangeInfo buyExchange, BigDecimal buyPrice,
                                      ExchangeInfo sellExchange, BigDecimal sellPrice,
                                      BigDecimal profitPercentage) {
        this.pair = pair;
        this.timestamp = timestamp;
        this.buyExchange = buyExchange;
        this.buyPrice = buyPrice;
        this.sellExchange = sellExchange;
        this.sellPrice = sellPrice;
        this.profitPercentage = profitPercentage;
    }

    // Getters
    public Long getId() { return id; }
    public CurrencyPair getPair() { return pair; }
    public Instant getTimestamp() { return timestamp; }
    public ExchangeInfo getBuyExchange() { return buyExchange; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public ExchangeInfo getSellExchange() { return sellExchange; }
    public BigDecimal getSellPrice() { return sellPrice; }
    public BigDecimal getProfitPercentage() { return profitPercentage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkedArbitrageOpportunity that = (LinkedArbitrageOpportunity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}