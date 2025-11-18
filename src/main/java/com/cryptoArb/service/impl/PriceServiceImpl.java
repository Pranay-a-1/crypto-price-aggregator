package com.cryptoArb.service.impl;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.repository.PriceTickRepository;
import com.cryptoArb.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PriceService.
 * Orchestrates fetching data from the database and aggregating it
 * into a consolidated price view.
 */
@Service
public class PriceServiceImpl implements PriceService {

    private final PriceTickRepository priceTickRepository;

    @Autowired
    public PriceServiceImpl(PriceTickRepository priceTickRepository) {
        this.priceTickRepository = priceTickRepository;
    }

    @Override
    public Optional<ConsolidatedPrice> getConsolidatedPriceForPair(CurrencyPair pair) {
        // 1. Fetch all ticks for this pair from the DB
        //    (Using the new query method we just added to the repository)
        List<PriceTick> ticks = priceTickRepository.findByPairBaseAndPairQuote(
                pair.getBase(),
                pair.getQuote()
        );

        if (ticks.isEmpty()) {
            return Optional.empty();
        }

        // 2. Aggregate the ticks to find the "Consolidated Price"
        //    (Logic ported from Part 1)
        return Optional.of(aggregateTicks(ticks, pair));
    }

    /**
     * Helper method to perform the aggregation logic.
     * Finds Best Bid (Highest), Best Ask (Lowest), and Latest Timestamp.
     *
     * @param ticks List of PriceTick objects to aggregate
     * @param pair CurrencyPair for which the ticks are aggregated
     * @return ConsolidatedPrice object containing the aggregated data
     *
     * This takes a list of PriceTick objects and a CurrencyPair object as input,
     * and returns a ConsolidatedPrice object containing the aggregated data.
     *
     * It first finds the tick with the highest bid price,
     * then the tick with the lowest ask price,
     * and finally the tick with the latest timestamp.
     *
     * It then returns a ConsolidatedPrice object containing the aggregated data.
     */
    private ConsolidatedPrice aggregateTicks(List<PriceTick> ticks, CurrencyPair pair) {
        // Find the tick with the HIGHEST bid price
        PriceTick bestBidTick = ticks.stream()
                .max(Comparator.comparing(PriceTick::getBidPrice))
                .orElseThrow(); // Should not happen as we checked isEmpty

        // Find the tick with the LOWEST ask price
        PriceTick bestAskTick = ticks.stream()
                .min(Comparator.comparing(PriceTick::getAskPrice))
                .orElseThrow();

        // Find the tick with the LATEST timestamp
        PriceTick latestTick = ticks.stream()
                .max(Comparator.comparing(PriceTick::getTimestamp))
                .orElseThrow();

        // Construct the result
        return new ConsolidatedPrice(
                pair,
                latestTick.getTimestamp(),
                bestBidTick.getBidPrice(),
                bestBidTick.getExchange(),
                bestAskTick.getAskPrice(),
                bestAskTick.getExchange()
        );
    }
}