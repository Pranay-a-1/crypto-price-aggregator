package com.cryptoArb.crypto_price_aggregator.controller;

import com.cryptoArb.crypto_price_aggregator.domain.ArbitrageOpportunity;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.service.ArbitrageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arbitrage")
@Validated
public class ArbitrageController {

    private static final Logger log = LoggerFactory.getLogger(ArbitrageController.class);

    private final ArbitrageService arbitrageService;

    public ArbitrageController(ArbitrageService arbitrageService) {
        this.arbitrageService = arbitrageService;
    }

    /**
     * Get recent arbitrage opportunities for a currency pair.
     *
     * @param base  Base currency (e.g., BTC)
     * @param quote Quote currency (e.g., USD)
     * @param limit Maximum number of opportunities to return (default 10)
     * @return List of recent arbitrage opportunities
     */
    @GetMapping("/{base}/{quote}")
    public ResponseEntity<List<ArbitrageOpportunity>> getOpportunities(
            @PathVariable @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z]{3,4}$", message = "Base currency must be 3-4 letters") String base,
            @PathVariable @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z]{3,4}$", message = "Quote currency must be 3-4 letters") String quote,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/arbitrage/{}/{} limit={}", base, quote, limit);

        CurrencyPair pair = CurrencyPair.of(base, quote);
        List<ArbitrageOpportunity> opportunities = arbitrageService.getRecentOpportunities(pair, limit);

        return ResponseEntity.ok(opportunities);
    }
}
