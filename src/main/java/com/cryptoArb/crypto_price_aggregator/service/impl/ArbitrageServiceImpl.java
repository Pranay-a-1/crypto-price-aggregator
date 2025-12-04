package com.cryptoArb.crypto_price_aggregator.service.impl;

import com.cryptoArb.crypto_price_aggregator.domain.ArbitrageOpportunity;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.repository.ArbitrageRepository;
import com.cryptoArb.crypto_price_aggregator.service.ArbitrageService;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ArbitrageService for detecting and managing arbitrage
 * opportunities.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Only handles arbitrage detection logic
 * - Dependency Inversion: Depends on abstractions (PriceService,
 * ArbitrageRepository)
 * - Open/Closed: Can extend with different detection algorithms
 * 
 * TDD: Developed test-first, with tests driving the implementation.
 * Manual Calculation: Uses BigDecimal for precise financial calculations (no
 * rounding errors).
 * Phase 10: Final phase bringing together all previous infrastructure for
 * business logic.
 */
@Service
@Transactional
public class ArbitrageServiceImpl implements ArbitrageService {

    private static final Logger log = LoggerFactory.getLogger(ArbitrageServiceImpl.class);

    private final PriceService priceService;
    private final ArbitrageRepository arbitrageRepository;

    public ArbitrageServiceImpl(PriceService priceService, ArbitrageRepository arbitrageRepository) {
        this.priceService = priceService;
        this.arbitrageRepository = arbitrageRepository;
    }

    @Override
    @Timed(value = "arbitrage.detection", description = "Time taken to detect arbitrage")
    public Optional<ArbitrageOpportunity> detectArbitrage(CurrencyPair pair) {
        log.debug("Detecting arbitrage opportunity for pair: {}", pair);

        // Fetch latest ticks from all exchanges
        Map<String, PriceTick> latestTicks = priceService.getLatestPriceTicks(pair);

        if (latestTicks.isEmpty() || latestTicks.size() < 2) {
            log.debug("Not enough exchanges to detect arbitrage for pair: {}", pair);
            return Optional.empty();
        }

        // Manual calculation: Find minimum ask and maximum bid
        Optional<PriceTick> minAskTick = latestTicks.values().stream()
                .min((t1, t2) -> t1.getAsk().compareTo(t2.getAsk()));

        Optional<PriceTick> maxBidTick = latestTicks.values().stream()
                .max((t1, t2) -> t1.getBid().compareTo(t2.getBid()));

        if (minAskTick.isEmpty() || maxBidTick.isEmpty()) {
            log.debug("Could not find valid ask/bid prices for pair: {}", pair);
            return Optional.empty();
        }

        PriceTick buyTick = minAskTick.get(); // Where to buy (lowest ask)
        PriceTick sellTick = maxBidTick.get(); // Where to sell (highest bid)

        BigDecimal buyPrice = buyTick.getAsk(); // We buy at the ask price
        BigDecimal sellPrice = sellTick.getBid(); // We sell at the bid price

        // Arbitrage exists if we can sell for more than we buy
        if (sellPrice.compareTo(buyPrice) > 0) {
            // Manual profit calculation using BigDecimal
            BigDecimal profit = sellPrice.subtract(buyPrice);
            BigDecimal profitPercentage = profit
                    .divide(buyPrice, 10, RoundingMode.HALF_UP) // Prevent infinite decimals
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(4, RoundingMode.HALF_UP);

            log.info("Arbitrage detected for {}: Buy at {} ({}) for {}, Sell at {} ({}) for {}, Profit: {}%",
                    pair, buyTick.getExchange(), buyTick.getExchange().name(), buyPrice,
                    sellTick.getExchange(), sellTick.getExchange().name(), sellPrice,
                    profitPercentage);

            // Create and persist arbitrage opportunity
            ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                    pair,
                    buyTick.getExchange(),
                    sellTick.getExchange(),
                    buyPrice,
                    sellPrice,
                    profitPercentage,
                    Instant.now());

            ArbitrageOpportunity saved = arbitrageRepository.save(opportunity);
            return Optional.of(saved);
        }

        log.debug("No arbitrage opportunity for {}: Best bid ({}) <= Best ask ({})",
                pair, sellPrice, buyPrice);
        return Optional.empty();
    }

    @Override
    public List<ArbitrageOpportunity> getRecentOpportunities(CurrencyPair pair, int limit) {
        log.debug("Fetching recent arbitrage opportunities for pair: {}, limit: {}", pair, limit);

        List<ArbitrageOpportunity> opportunities = arbitrageRepository
                .findByCurrencyPair_BaseAndCurrencyPair_QuoteOrderByDetectedAtDesc(
                        pair.getBase(), pair.getQuote());

        // Apply limit
        return opportunities.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
