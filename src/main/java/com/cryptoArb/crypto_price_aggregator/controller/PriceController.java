package com.cryptoArb.crypto_price_aggregator.controller;

import com.cryptoArb.crypto_price_aggregator.domain.AggregatedTopOfBookQuote;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for price endpoints.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Only handles HTTP concerns, delegates to service
 * - Dependency Inversion: Depends on PriceService abstraction
 * <p>
 * Following RESTful design:
 * - GET /api/prices/{base}/{quote} - Fetch aggregated price
 * <p>
 * Error Handling:
 * - 200 OK: Price found
 * - 400 BAD REQUEST: Invalid input (empty base/quote)
 * - 404 NOT FOUND: Price not available
 * - 500 INTERNAL SERVER ERROR: Unexpected error
 */
@RestController
@RequestMapping("/api/prices")
@Validated
public class PriceController {

    private static final Logger log = LoggerFactory.getLogger(PriceController.class);

    private final PriceService priceService;

    /**
     * Constructor with dependency injection.
     *
     * @param priceService The price service for fetching prices
     */
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    /**
     * Get AggregatedTopOfBookQuote for a currency pair.
     * AggregatedTopOfBookQuote here means the best bestBid and bestAsk across all
     * exchanges.
     *
     * @param base  Base currency (e.g., BTC)
     * @param quote Quote currency (e.g., USD)
     * @return ResponseEntity with AggregatedTopOfBookQuote or error status
     */
    @GetMapping("/{base}/{quote}")
    public ResponseEntity<AggregatedTopOfBookQuote> getPrice(
            @PathVariable @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z]{3,4}$", message = "Base currency must be 3-4 letters") String base,
            @PathVariable @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z]{3,4}$", message = "Quote currency must be 3-4 letters") String quote) {

        log.info("GET /api/prices/{}/{}", base, quote);

        try {
            // Validate input (fail-fast)
            if (base == null || base.isBlank()) {
                log.warn("Invalid request: base currency is empty");
                return ResponseEntity.badRequest().build();
            }
            if (quote == null || quote.isBlank()) {
                log.warn("Invalid request: quote currency is empty");
                return ResponseEntity.badRequest().build();
            }

            // Create currency pair (validation happens in factory method)
            CurrencyPair pair = CurrencyPair.of(base, quote);

            // Fetch AggregatedTopOfBookQuote
            Optional<AggregatedTopOfBookQuote> price = priceService.getAggregatedTopOfBookQuote(pair);

            // Return 404 if not found, 200 if found
            return price
                    .map(p -> {
                        log.info("Price found for {}: bestBid={}, bestAsk={}", pair, p.bestBid(), p.bestAsk());
                        return ResponseEntity.ok(p);
                    })
                    .orElseGet(() -> {
                        log.warn("Price not found for {}", pair);
                        return ResponseEntity.notFound().build();
                    });

        } catch (IllegalArgumentException e) {
            // Invalid currency pair (e.g., empty base/quote)
            log.error("Invalid currency pair: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            // Unexpected error
            log.error("Unexpected error fetching price for {}/{}: {}",
                    base, quote, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get individual price ticks from all exchanges for a currency pair.
     *
     * @param base  Base currency (e.g., BTC)
     * @param quote Quote currency (e.g., USD)
     * @return ResponseEntity with Map of Exchange -> PriceTick
     */
    @GetMapping("/{base}/{quote}/exchanges")
    public ResponseEntity<Map<String, PriceTick>> getExchangePrices(
            @PathVariable String base,
            @PathVariable String quote) {

        log.info("GET /api/prices/{}/{}/exchanges", base, quote);

        try {
            // Validate input
            if (base == null || base.isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            if (quote == null || quote.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            CurrencyPair pair = new CurrencyPair(base, quote);
            Map<String, PriceTick> prices = priceService.getLatestPriceTicks(pair);

            return ResponseEntity.ok(prices);

        } catch (IllegalArgumentException e) {
            log.error("Invalid currency pair: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error fetching exchange prices for {}/{}: {}",
                    base, quote, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
