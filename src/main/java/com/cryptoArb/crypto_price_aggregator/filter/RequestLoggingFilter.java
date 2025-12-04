package com.cryptoArb.crypto_price_aggregator.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Request Logging Filter with Correlation ID support
 *
 * Manual Implementation (Phase 8 - Observability):
 * - Adds unique request ID to MDC for distributed tracing
 * - Logs incoming HTTP requests
 * - Ensures MDC cleanup in finally block
 *
 * Why Manual: Direct MDC manipulation demonstrates how correlation IDs work
 * before using Spring's abstractions (e.g., Sleuth/Micrometer Tracing)
 *
 * Design Decisions:
 * - UUID for guaranteed uniqueness across distributed systems
 * - try-finally ensures MDC cleanup even on exceptions
 * - Implements Filter interface for Servlet-based filtering
 */
@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Generate or retrieve request ID
        String requestId = generateRequestId(httpRequest);

        try {
            // Add request ID to MDC for logging
            MDC.put(REQUEST_ID_MDC_KEY, requestId);

            // Log incoming request
            log.info("Incoming request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            // Continue filter chain
            chain.doFilter(request, response);

        } finally {
            // CRITICAL: Always clear MDC to prevent memory leaks and cross-request
            // contamination
            // This must be in finally block to execute even when exceptions occur
            MDC.clear();
        }
    }

    /**
     * Generate unique request ID
     * Checks for existing X-Request-ID header (from upstream services)
     * Otherwise generates new UUID
     */
    private String generateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("RequestLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        log.info("RequestLoggingFilter destroyed");
    }
}
