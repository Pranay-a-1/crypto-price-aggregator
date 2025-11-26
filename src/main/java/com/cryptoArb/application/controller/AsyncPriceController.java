package com.cryptoArb.application.controller;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.ReportResult;
import com.cryptoArb.service.PriceReportService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Phase 14: Async API Controller with DeferredResult.
 * 
 * Demonstrates Spring MVC asynchronous request processing patterns.
 * Uses DeferredResult to implement long-polling without blocking web container
 * threads.
 * 
 * Design Patterns:
 * - DeferredResult: Non-blocking async request/response
 * - Long-polling: Client waits for server event
 * - Request-response correlation: UUID tracking
 * 
 * Architecture Benefits:
 * - Frees Tomcat/Undertow worker threads (scalability)
 * - Reduces client polling frequency (network efficiency)
 * - Handles timeouts gracefully (user experience)
 * 
 * Use Cases:
 * - Async report generation with progress checking
 * - Waiting for real-time price updates
 * - Long-running queries/calculations
 */
@RestController
@RequestMapping("/api/v1/async")
public class AsyncPriceController {

    private static final Logger log = LoggerFactory.getLogger(AsyncPriceController.class);

    private final PriceReportService priceReportService;
    private final ExecutorService asyncExecutor;
    private final Counter asyncRequestCounter;
    private final Counter asyncTimeoutCounter;

    // In-memory storage for pending async results
    // In production, use Redis or similar distributed cache
    private final Map<String, CompletableFuture<ReportResult>> pendingReports = new ConcurrentHashMap<>();

    @Value("${cpa.async.timeout-seconds:30}")
    private long timeoutSeconds;

    @Autowired
    public AsyncPriceController(PriceReportService priceReportService, MeterRegistry meterRegistry) {
        this.priceReportService = priceReportService;
        this.asyncExecutor = Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r);
            t.setName("async-api-" + t.getId());
            return t;
        });

        // Initialize metrics
        this.asyncRequestCounter = Counter.builder("api.async.requests")
                .description("Total number of async API requests")
                .register(meterRegistry);

        this.asyncTimeoutCounter = Counter.builder("api.async.timeouts")
                .description("Total number of async requests that timed out")
                .register(meterRegistry);
    }

    /**
     * Triggers async report generation and returns immediately with request ID.
     * 
     * Flow:
     * 1. Client calls POST /api/v1/async/report/BTC-USD
     * 2. Server starts async report generation
     * 3. Returns immediately with requestId and HTTP 202 Accepted
     * 4. Client polls GET /api/v1/async/result/{requestId} to check completion
     * 
     * Why POST (not GET)?
     * - Triggers server-side action (report generation)
     * - Not idempotent (each call creates new report)
     * - Follows REST semantics for state-changing operations
     * 
     * @param pairString Currency pair in format "BTC-USD"
     * @return Response with requestId and status
     */
    @PostMapping("/report/{pair}")
    public ResponseEntity<Map<String, Object>> startAsyncReport(@PathVariable("pair") String pairString) {
        log.info("Received async report request for pair: {}", pairString);
        asyncRequestCounter.increment();

        // Parse currency pair
        String[] parts = pairString.split("-");
        if (parts.length != 2) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid currency pair format. Use: BTC-USD"));
        }

        CurrencyPair pair = new CurrencyPair(parts[0], parts[1]);

        // Generate unique request ID for tracking
        String requestId = UUID.randomUUID().toString();

        // Start async report generation
        CompletableFuture<ReportResult> futureResult = priceReportService.generateReport(pair);
        pendingReports.put(requestId, futureResult);

        // Clean up completed reports after retrieval
        futureResult.whenComplete((result, ex) -> {
            log.debug("Report {} completed with status: {}",
                    requestId, result != null ? result.status() : "ERROR");
        });

        // Return immediately (HTTP 202 Accepted)
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "requestId", requestId,
                        "status", "PROCESSING",
                        "message", "Report generation started",
                        "checkUrl", "/api/v1/async/result/" + requestId));
    }

    /**
     * Checks the status of an async operation using DeferredResult.
     * 
     * This endpoint uses long-polling:
     * - If result is ready: Returns immediately with data
     * - If result is pending: Waits up to timeout (30s) for completion
     * - If timeout: Returns HTTP 408 Request Timeout
     * 
     * Why DeferredResult?
     * - Doesn't block web container threads while waiting
     * - Container thread is released immediately after this method returns
     * - Result is completed on different thread when data is ready
     * - Handles timeout automatically
     * 
     * Alternative: CompletableFuture (Spring 5.3+)
     * 
     * @GetMapping
     *             public CompletableFuture<ResponseEntity<?>> getResult(...) {
     *             return futureResult; }
     * 
     *             Current choice: DeferredResult for explicit timeout control
     * 
     * @param requestId UUID of the async operation
     * @return DeferredResult that will complete when data is ready or timeout
     */
    @GetMapping("/result/{requestId}")
    public DeferredResult<ResponseEntity<Map<String, Object>>> getAsyncResult(
            @PathVariable String requestId) {

        log.info("Checking status for async request: {}", requestId);

        // Create DeferredResult with timeout
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = new DeferredResult<>(
                timeoutSeconds * 1000);

        // Handle timeout
        deferredResult.onTimeout(() -> {
            log.warn("Async request {} timed out after {}s", requestId, timeoutSeconds);
            asyncTimeoutCounter.increment();
            deferredResult.setResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(Map.of(
                            "status", "TIMEOUT",
                            "message", "Request timed out. Result may still be processing.")));
        });

        // Retrieve pending result
        CompletableFuture<ReportResult> futureResult = pendingReports.get(requestId);

        if (futureResult == null) {
            // Request ID not found
            deferredResult.setResult(ResponseEntity.notFound().build());
            return deferredResult;
        }

        // Check if result is already complete
        if (futureResult.isDone()) {
            try {
                ReportResult result = futureResult.get();
                pendingReports.remove(requestId); // Clean up

                deferredResult.setResult(ResponseEntity.ok(Map.of(
                        "status", result.status(),
                        "reportType", result.reportType(),
                        "totalTicks", result.totalTicks(),
                        "avgPrice", result.avgPrice())));
            } catch (Exception e) {
                log.error("Error retrieving result for {}: {}", requestId, e.getMessage());
                deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error retrieving result")));
            }
        } else {
            // Result is still pending - wait asynchronously
            asyncExecutor.submit(() -> {
                try {
                    // This wait happens on async thread, NOT web container thread
                    ReportResult result = futureResult.get();
                    pendingReports.remove(requestId); // Clean up

                    deferredResult.setResult(ResponseEntity.ok(Map.of(
                            "status", result.status(),
                            "reportType", result.reportType(),
                            "totalTicks", result.totalTicks(),
                            "avgPrice", result.avgPrice())));

                } catch (Exception e) {
                    log.error("Error waiting for result {}: {}", requestId, e.getMessage());
                    if (!deferredResult.isSetOrExpired()) {
                        deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Error processing request")));
                    }
                }
            });
        }

        return deferredResult;
    }

    /**
     * Cleanup method called on bean destruction.
     * Shuts down the async executor gracefully.
     */
    @jakarta.annotation.PreDestroy
    public void cleanup() {
        log.info("Shutting down async executor");
        asyncExecutor.shutdown();
    }

    /*
     * DESIGN DECISIONS AND TRADE-OFFS:
     * 
     * 1. DeferredResult vs CompletableFuture (Controller Return Type)
     * ----------------------------------------------------------------
     * CHOSEN: DeferredResult
     * 
     * Pros:
     * - Explicit timeout control (set via constructor)
     * - Callback hooks (onTimeout, onCompletion, onError)
     * - Community resources (been around since Spring 3.2)
     * 
     * Cons:
     * - More verbose than CompletableFuture
     * - Manual result setting required
     * 
     * Alternative: Return CompletableFuture<ResponseEntity<?>>
     * Pros: Simpler code, automatic result handling
     * Cons: Less explicit timeout control, Spring handles internally
     * 
     * When to use each:
     * - DeferredResult: Need explicit timeout handling, custom hooks
     * - CompletableFuture: Simpler cases, trust Spring's defaults
     * 
     * 
     * 2. In-Memory Storage vs Redis
     * ------------------------------
     * CURRENT: ConcurrentHashMap (in-memory)
     * 
     * Pros:
     * - Simple, no external dependencies
     * - Fast access (no network)
     * - Good for demo/development
     * 
     * Cons:
     * - Lost on server restart
     * - Doesn't work in multi-instance deployments
     * - No TTL (manual cleanup required)
     * 
     * PRODUCTION: Redis with TTL
     * Pros: Distributed, persistent, automatic expiration
     * Implementation: Use Spring Data Redis to store requestId -> resultData
     * 
     * 
     * 3. Long-Polling vs Server-Sent Events (SSE) vs WebSockets
     * ----------------------------------------------------------
     * CHOSEN: Long-Polling with DeferredResult
     * 
     * Long-Polling:
     * - Pros: Works with any HTTP client, firewall-friendly
     * - Cons: Reconnection overhead, not true real-time
     * - Use when: Infrequent updates, simple clients
     * 
     * SSE (Server-Sent Events):
     * - Pros: True streaming, automatic reconnection
     * - Cons: One-way (server -> client only)
     * - Use when: Continuous updates (live prices, notifications)
     * 
     * WebSockets:
     * - Pros: Full-duplex, lowest latency
     * - Cons: Complex (requires WebSocket client), proxy challenges
     * - Use when: Bidirectional real-time (chat, gaming)
     * 
     * Phase 15 will demonstrate WebSockets for live price streaming.
     */
}
