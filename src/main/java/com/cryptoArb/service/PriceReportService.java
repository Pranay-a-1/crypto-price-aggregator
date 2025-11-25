package com.cryptoArb.service;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.domain_spring.ReportResult;
import com.cryptoArb.repository.PriceTickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Phase 14: Price Report Service with @Async support.
 * 
 * Demonstrates Spring's @Async functionality by executing long-running report
 * generation tasks asynchronously on a custom thread pool. This showcases:
 * - Non-blocking execution (caller doesn't wait)
 * - Custom thread pool usage (async-report-* threads)
 * - CompletableFuture for result access and composition
 * 
 * Design Decisions:
 * - @Async("taskExecutor"): Explicitly specify custom executor from AsyncConfig
 * - CompletableFuture return type: Allows caller to compose/chain operations
 * - Thread.sleep() simulation: Demonstrates async behavior observably
 * - Stateless service: Thread-safe for concurrent execution
 * 
 * Use Cases:
 * - Generating price summary reports in background
 * - PDF/Excel report export (simulated here)
 * - Analytics/aggregation tasks that don't need immediate response
 */
@Service
public class PriceReportService {

    private static final Logger log = LoggerFactory.getLogger(PriceReportService.class);

    private final PriceTickRepository priceTickRepository;

    @Autowired
    public PriceReportService(PriceTickRepository priceTickRepository) {
        this.priceTickRepository = priceTickRepository;
    }

    /**
     * Generates a price summary report asynchronously.
     * 
     * This method demonstrates several @Async patterns:
     * 1. Returns immediately to caller (non-blocking)
     * 2. Executes on custom thread pool (async-report-* threads)
     * 3. Returns CompletableFuture for result access
     * 4. Simulates long-running work with Thread.sleep()
     * 
     * Execution Flow:
     * 1. Method is called by client (on client thread)
     * 2. Spring creates proxy that submits task to executor
     * 3. Method returns CompletableFuture immediately (< 1ms)
     * 4. Actual work executes on async-report-* thread
     * 5. CompletableFuture completes when work finishes
     * 
     * Why @Async("taskExecutor")?
     * - Explicitly specifies which executor to use
     * - If omitted, would use default executor (not our custom one)
     * - Makes configuration visible and testable
     * 
     * Why CompletableFuture<ReportResult>?
     * - Caller can await completion: future.get() or Awaitility
     * - Caller can chain operations: future.thenApply(...)
     * - Better than void for demonstrating async behavior in tests
     * 
     * @param pair Currency pair to generate report for
     * @return CompletableFuture containing report result when complete
     */
    @Async("taskExecutor")
    public CompletableFuture<ReportResult> generateReport(CurrencyPair pair) {
        String threadName = Thread.currentThread().getName();
        log.info("Starting async report generation for {}/{} on thread: {}",
                pair.getBase(), pair.getQuote(), threadName);

        try {
            // Simulate long-running report generation (database query, aggregation, PDF
            // creation, etc.)
            // In production, this would be actual CPU/IO-intensive work
            log.debug("Simulating long-running work with 3-second delay...");
            Thread.sleep(3000);

            // Query database for price ticks for this pair
            log.debug("Querying database for PriceTicks with base={}, quote={}",
                    pair.getBase(), pair.getQuote());

            List<PriceTick> ticks = priceTickRepository
                    .findByPairBaseAndPairQuote(pair.getBase(), pair.getQuote());

            log.debug("Found {} price ticks for {}/{}",
                    ticks.size(), pair.getBase(), pair.getQuote());

            // Calculate statistics
            long totalTicks = ticks.size();
            BigDecimal avgPrice = calculateAveragePrice(ticks);

            log.info("Report generation completed on thread: {}. Ticks: {}, Avg Price: {}",
                    threadName, totalTicks, avgPrice);

            // Create success result
            ReportResult result = ReportResult.completed("PRICE_SUMMARY", totalTicks, avgPrice);

            // Wrap in CompletableFuture and return
            return CompletableFuture.completedFuture(result);

        } catch (InterruptedException e) {
            log.error("Report generation interrupted on thread: {}", threadName, e);
            Thread.currentThread().interrupt(); // Restore interrupt status

            // Create failure result
            ReportResult errorResult = ReportResult.failed("PRICE_SUMMARY",
                    "Report generation interrupted: " + e.getMessage());
            return CompletableFuture.completedFuture(errorResult);

        } catch (Exception e) {
            log.error("Unexpected error during report generation on thread: {}", threadName, e);

            // Create failure result
            ReportResult errorResult = ReportResult.failed("PRICE_SUMMARY",
                    "Unexpected error: " + e.getMessage());
            return CompletableFuture.completedFuture(errorResult);
        }
    }

    /**
     * Calculates average price from a list of price ticks.
     * 
     * Uses bid price for calculation (could also use ask or midpoint).
     * Returns ZERO if list is empty to avoid division by zero.
     * 
     * Time Complexity: O(n) where n = ticks.size()
     * Space Complexity: O(1) - single accumulator
     * 
     * @param ticks List of price ticks
     * @return Average bid price, or ZERO if no ticks
     */
    private BigDecimal calculateAveragePrice(List<PriceTick> ticks) {
        if (ticks.isEmpty()) {
            log.warn("No ticks provided for average calculation, returning ZERO");
            return BigDecimal.ZERO;
        }

        // Sum all bid prices
        BigDecimal sum = ticks.stream()
                .map(PriceTick::getBidPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average with 2 decimal places
        BigDecimal count = BigDecimal.valueOf(ticks.size());
        BigDecimal average = sum.divide(count, 2, RoundingMode.HALF_UP);

        log.debug("Calculated average price: {} from {} ticks", average, ticks.size());

        return average;
    }

    /*
     * Alternative Implementation Approaches:
     * 
     * 1. Void Return Type (Fire-and-Forget):
     * 
     * @Async
     * public void generateReport(CurrencyPair pair) { ... }
     * - Pros: Simpler, caller truly doesn't care about result
     * - Cons: Can't test completion, can't access result, limits use cases
     * 
     * 2. ListenableFuture (Spring's Alternative):
     * 
     * @Async
     * public ListenableFuture<ReportResult> generateReport(...) { ... }
     * - Pros: Spring-native, callbacks via addCallback()
     * - Cons: CompletableFuture is more idiomatic in modern Java
     * 
     * 3. Virtual Threads (Java 21+):
     * No @Async needed, just run in virtual thread executor
     * - Pros: Millions of lightweight threads, no pool tuning
     * - Cons: Requires Java 21+ (this project uses Java 17)
     * 
     * Current choice: CompletableFuture provides best balance for Java 17
     * and Spring integration, with full Future capabilities for composition.
     */
}
