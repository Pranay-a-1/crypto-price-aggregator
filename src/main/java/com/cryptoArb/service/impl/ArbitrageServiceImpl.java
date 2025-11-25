package com.cryptoArb.service.impl;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.Exchange;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.repository.ArbitrageRepository;
import com.cryptoArb.repository.PriceTickRepository;
import com.cryptoArb.service.ArbitrageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ArbitrageService.
 * Handles business logic for retrieving and managing arbitrage opportunities.
 */
@Service
@Transactional(readOnly = true)
public class ArbitrageServiceImpl implements ArbitrageService {

    private static final Logger log = LoggerFactory.getLogger(ArbitrageServiceImpl.class);

    private final ArbitrageRepository arbitrageRepository;
    private final PriceTickRepository priceTickRepository;

    // Default look back period is 5 minutes
    private static final Duration DEFAULT_LOOK_BACK = Duration.ofMinutes(5);

    // Time window for arbitrage detection (60 seconds)
    private static final Duration ARBITRAGE_WINDOW = Duration.ofSeconds(60);

    @Autowired
    public ArbitrageServiceImpl(ArbitrageRepository arbitrageRepository,
            PriceTickRepository priceTickRepository) {
        this.arbitrageRepository = arbitrageRepository;
        this.priceTickRepository = priceTickRepository;
    }

    /**
     * Retrieves opportunities from the last 5 minutes.
     */
    @Override
    public List<ArbitrageOpportunity> getRecentOpportunities() {
        // Delegate to the parameterized method with the default 5-minute window
        return getRecentOpportunities(DEFAULT_LOOK_BACK);
    }

    /**
     * Retrieves opportunities from the last [duration].
     */
    @Override
    public List<ArbitrageOpportunity> getRecentOpportunities(Duration duration) {
        // 1. Calculate the cutoff time based on the provided duration
        Instant cutoffTime = Instant.now().minus(duration);

        // 2. Query the repository
        return arbitrageRepository.findByTimestampAfter(cutoffTime);
    }

    /**
     * Detects and saves arbitrage opportunities for a given currency pair.
     * 
     * Algorithm:
     * 1. Query recent PriceTicks (last 60 seconds) for the pair
     * 2. Group by exchange
     * 3. Find highest bid (sell exchange) and lowest ask (buy exchange)
     * 4. If bid > ask, create and save ArbitrageOpportunity
     * 
     * @param pair The currency pair to analyze
     */
    @Transactional(readOnly = false)
    public void detectAndSaveOpportunities(CurrencyPair pair) {
        log.debug("Detecting arbitrage opportunities for {}/{}", pair.getBase(), pair.getQuote());

        // Step 1: Query recent price ticks (last 60 seconds)
        Instant cutoffTime = Instant.now().minus(ARBITRAGE_WINDOW);
        List<PriceTick> recentTicks = priceTickRepository
                .findByPairBaseAndPairQuoteAndTimestampAfter(
                        pair.getBase(), pair.getQuote(), cutoffTime);

        if (recentTicks.isEmpty()) {
            log.debug("No recent ticks found for {}/{}", pair.getBase(), pair.getQuote());
            return;
        }

        // Step 2: Group ticks by exchange and get the most recent tick per exchange
        Map<String, PriceTick> latestTicksByExchange = recentTicks.stream()
                .collect(Collectors.toMap(
                        tick -> tick.getExchange().getId(),
                        tick -> tick,
                        (existing, replacement) -> existing.getTimestamp().isAfter(replacement.getTimestamp())
                                ? existing
                                : replacement));

        if (latestTicksByExchange.size() < 2) {
            log.debug("Need at least 2 exchanges for arbitrage, found: {}",
                    latestTicksByExchange.size());
            return;
        }

        // Step 3: Find exchange with highest bid (where we can sell)
        PriceTick sellTick = latestTicksByExchange.values().stream()
                .max((t1, t2) -> t1.getBidPrice().compareTo(t2.getBidPrice()))
                .orElse(null);

        // Step 4: Find exchange with lowest ask (where we can buy)
        PriceTick buyTick = latestTicksByExchange.values().stream()
                .min((t1, t2) -> t1.getAskPrice().compareTo(t2.getAskPrice()))
                .orElse(null);

        if (sellTick == null || buyTick == null) {
            log.warn("Could not find buy/sell ticks for arbitrage detection");
            return;
        }

        // Step 5: Check if arbitrage opportunity exists (sell price > buy price)
        BigDecimal sellPrice = sellTick.getBidPrice();
        BigDecimal buyPrice = buyTick.getAskPrice();

        if (sellPrice.compareTo(buyPrice) > 0) {
            // Calculate profit percentage: (sellPrice - buyPrice) / buyPrice * 100
            BigDecimal profit = sellPrice.subtract(buyPrice);
            BigDecimal profitPercentage = profit
                    .divide(buyPrice, 10, RoundingMode.HALF_UP);

            // Create and save opportunity
            ArbitrageOpportunity opportunity = ArbitrageOpportunity.builder()
                    .pair(pair)
                    .timestamp(Instant.now())
                    .buyExchange(buyTick.getExchange())
                    .buyPrice(buyPrice)
                    .sellExchange(sellTick.getExchange())
                    .sellPrice(sellPrice)
                    .profitPercentage(profitPercentage)
                    .build();

            arbitrageRepository.save(opportunity);

            log.info("Arbitrage opportunity detected! Buy at {} for {}, Sell at {} for {}, Profit: {}%",
                    buyTick.getExchange().getId(), buyPrice,
                    sellTick.getExchange().getId(), sellPrice,
                    profitPercentage.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        } else {
            log.debug("No arbitrage opportunity: highest bid ({}) <= lowest ask ({})",
                    sellPrice, buyPrice);
        }
    }
}