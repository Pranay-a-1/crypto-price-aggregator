package com.cryptoArb.controller;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.PriceService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class PriceGraphQLController {

    private final PriceService priceService;
    private final ArbitrageService arbitrageService;

    public PriceGraphQLController(PriceService priceService, ArbitrageService arbitrageService) {
        this.priceService = priceService;
        this.arbitrageService = arbitrageService;
    }

    @QueryMapping
    public Optional<ConsolidatedPrice> getPrice(@Argument String pair) {
        String[] parts = pair.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid currency pair format. Expected 'BASE-QUOTE'");
        }
        return priceService.getConsolidatedPriceForPair(new CurrencyPair(parts[0], parts[1]));
    }

    @QueryMapping
    public List<ArbitrageOpportunity> getArbitrageOpportunities() {
        return arbitrageService.getRecentOpportunities();
    }
}
