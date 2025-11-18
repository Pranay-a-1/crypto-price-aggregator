package com.cryptoArb.service.impl;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import com.cryptoArb.repository.ArbitrageRepository;
import com.cryptoArb.service.ArbitrageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Implementation of ArbitrageService.
 * Handles business logic for retrieving and managing arbitrage opportunities.
 */
@Service
public class ArbitrageServiceImpl implements ArbitrageService {

    private final ArbitrageRepository arbitrageRepository;

    // Default look back period is 5 minutes
    private static final Duration DEFAULT_LOOK_BACK = Duration.ofMinutes(5);

    @Autowired
    public ArbitrageServiceImpl(ArbitrageRepository arbitrageRepository) {
        this.arbitrageRepository = arbitrageRepository;
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
}