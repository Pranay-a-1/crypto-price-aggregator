package com.cryptoArb.controller;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.exception.PriceNotFoundException;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.PriceService;
import com.cryptoArb.validation.ValidCurrencyPair;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST API Controller for exposing price and arbitrage data.
 *
 * Refactored Phase 12: Uses Global Exception Handling. , Declarative Validation
 */
@RestController // (1) Marks this class as a Spring-managed REST controller
@RequestMapping("/api/v1") // (2) Sets the base path for all endpoints in this class
@Tag(name = "Market Data", description = "Endpoints for real-time price and arbitrage data")
@Validated // (1) Enables method-level validation for @PathVariable constraints
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
    // http://localhost:8080/v3/api-docs
    //  http://localhost:8080/swagger-ui/index.html
    //    http://localhost:8080/swagger-ui.html
    @Operation(summary = "Get Consolidated Price", description = "Retrieves the best bid and ask price for a specific currency pair.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved price",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsolidatedPrice.class))),
            @ApiResponse(responseCode = "404", description = "Currency pair not found or no data available"),
            @ApiResponse(responseCode = "400", description = "Invalid currency pair format (use BASE-QUOTE)")
    })
    @GetMapping("/price/{pair}")
    public ResponseEntity<ConsolidatedPrice> getPriceForPair(
            @PathVariable @ValidCurrencyPair String pair) { // (2) Apply our custom validator

        // (3) Manual validation logic REMOVED.
        // If we get here, 'pair' is guaranteed to match "XXX-YYY".

        String[] parts = pair.split("-");
        // We can safely assume length is 2 because of the regex validation
        CurrencyPair currencyPair = new CurrencyPair(parts[0], parts[1]);

        // Call service
        Optional<ConsolidatedPrice> price = priceService.getConsolidatedPriceForPair(currencyPair);

        // --- REFACTORED SECTION ---
        // Old: .orElseGet(() -> ResponseEntity.notFound().build()); // this used to return 404
        // New: Throw exception to delegate to @ControllerAdvice // this throws PriceNotFoundException
        return price
                .map(ResponseEntity::ok) // If present, wrap in ResponseEntity.ok()
                .orElseThrow(() -> new PriceNotFoundException("Price not found for pair: " + pair));
    }



    /**
     * (8) The endpoint for retrieving arbitrage opportunities.
     * It maps HTTP GET requests for /api/v1/arbitrage to this method.
     * This fulfills requirement FS-8 from the SRS.
     */
    @Operation(summary = "Get Arbitrage Opportunities", description = "Retrieves all arbitrage opportunities detected in the last window.")
    @ApiResponse(responseCode = "200", description = "List of opportunities found")
    @GetMapping("/arbitrage")
    public ResponseEntity<List<ArbitrageOpportunity>> getRecentOpportunities() {
        // (9) Call the service layer to get the data.
        // In our test, this calls the mock. In production, it calls the real impl.
        List<ArbitrageOpportunity> opportunities = arbitrageService.getRecentOpportunities();

        // (10) Return the list wrapped in a 200 OK response.
        // Spring Boot's Jackson library will automatically serialize the list
        // into a JSON array (e.g., "[]").
        return ResponseEntity.ok(opportunities);
    }
}