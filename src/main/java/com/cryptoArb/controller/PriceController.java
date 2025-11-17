package com.cryptoArb.controller;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST API Controller for exposing price and arbitrage data.
 *
 * This is the minimal implementation to pass the PriceControllerTest.
 */
@RestController // (1) Marks this class as a Spring-managed REST controller
@RequestMapping("/api/v1") // (2) Sets the base path for all endpoints in this class
public class PriceController {

    // --- Dependencies ---
    private final PriceService priceService;
    private final ArbitrageService arbitrageService;

    /**
     * (3) Use constructor injection to autowire our service interfaces.
     * This is the modern standard for Spring dependency injection.
     */
    @Autowired
    public PriceController(PriceService priceService, ArbitrageService arbitrageService) {
        this.priceService = priceService;
        this.arbitrageService = arbitrageService;
    }

    /**
     * (4) The endpoint our test is looking for.
     * It maps HTTP GET requests for /api/v1/price/{pair} to this method.
     */
    @GetMapping("/price/{pair}")
    public ResponseEntity<ConsolidatedPrice> getPriceForPair(@PathVariable String pair) {

        // (5) Minimal logic to parse the path variable.
        // We will need to add proper error handling for a malformed pair
        // (e.g., "BTC-USD-EUR") in a future refactor, but for TDD,
        // we do the minimum.
        String[] parts = pair.split("-");
        if (parts.length != 2) {
            // Not a valid pair, return 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
        CurrencyPair currencyPair = new CurrencyPair(parts[0], parts[1]);

        // (6) Call our mockable service
        Optional<ConsolidatedPrice> price = priceService.getConsolidatedPriceForPair(currencyPair);

        // (7) Use ResponseEntity to return 200 OK or 404 Not Found
        // This directly matches the logic in our test.
        return price
                .map(ResponseEntity::ok) // If present, wrap in ResponseEntity.ok()
                .orElseGet(() -> ResponseEntity.notFound().build()); // If empty, return 404
    }
}