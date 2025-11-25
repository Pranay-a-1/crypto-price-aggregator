package com.cryptoArb.domain_spring;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Phase 14: Result object for async report generation.
 * 
 * This record encapsulates the results of an asynchronous price report
 * generation task.
 * Using a record ensures immutability (thread-safety) and provides clean value
 * semantics
 * for testing and API responses.
 * 
 * Design Decisions:
 * - Record Type: Immutable by default, perfect for DTOs passed between async
 * boundaries
 * - BigDecimal for avgPrice: Maintains precision for financial calculations
 * - Instant for timestamp: Standard temporal type for audit trails
 * - Status field: Enables future expansion for FAILED/PARTIAL states
 */
public record ReportResult(
        String reportId,
        Instant generatedAt,
        String reportType,
        Long totalTicks,
        BigDecimal avgPrice,
        String status) {

    /**
     * Factory method for creating a successful report result.
     * 
     * @param reportType Type of report (e.g., "PRICE_SUMMARY")
     * @param totalTicks Number of price ticks processed
     * @param avgPrice   Average price calculated from ticks
     * @return A completed ReportResult with status "COMPLETED"
     */
    public static ReportResult completed(String reportType, Long totalTicks, BigDecimal avgPrice) {
        return new ReportResult(
                java.util.UUID.randomUUID().toString(),
                Instant.now(),
                reportType,
                totalTicks,
                avgPrice,
                "COMPLETED");
    }

    /**
     * Factory method for creating a failed report result.
     * 
     * @param reportType   Type of report that was attempted
     * @param errorMessage Error message (stored in reportId for simplicity)
     * @return A failed ReportResult with status "FAILED"
     */
    public static ReportResult failed(String reportType, String errorMessage) {
        return new ReportResult(
                errorMessage,
                Instant.now(),
                reportType,
                0L,
                BigDecimal.ZERO,
                "FAILED");
    }
}
