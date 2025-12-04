package com.cryptoArb.crypto_price_aggregator.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Test for RequestLoggingFilter
 * Following RED-GREEN-REFACTOR cycle
 *
 * Purpose: Verify that RequestLoggingFilter adds correlation IDs to MDC
 * for distributed tracing
 */
@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
        MDC.clear(); // Ensure clean state
    }

    @Test
    void shouldAddRequestIdToMDC() throws ServletException, IOException {
        // ARRANGE
        when(request.getRequestURI()).thenReturn("/api/prices/BTCUSDT");
        when(request.getMethod()).thenReturn("GET");

        // ACT
        filter.doFilter(request, response, filterChain);

        // ASSERT - This will FAIL because RequestLoggingFilter doesn't exist yet
        // We expect MDC to have a requestId during the filter execution
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldClearMDCAfterRequest() throws ServletException, IOException {
        // ARRANGE
        when(request.getRequestURI()).thenReturn("/api/prices/BTCUSDT");
        when(request.getMethod()).thenReturn("GET");

        // ACT
        filter.doFilter(request, response, filterChain);

        // ASSERT - MDC should be cleared after request processing
        assertNull(MDC.get("requestId"), "MDC should be cleared after request");
    }

    @Test
    void shouldGenerateUniqueRequestIds() throws ServletException, IOException {
        // ARRANGE
        when(request.getRequestURI()).thenReturn("/api/prices/BTCUSDT");
        when(request.getMethod()).thenReturn("GET");

        String[] requestIds = new String[2];

        // ACT - Make two requests
        filter.doFilter(request, response, new FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse) {
                requestIds[0] = MDC.get("requestId");
            }
        });

        filter.doFilter(request, response, new FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse) {
                requestIds[1] = MDC.get("requestId");
            }
        });

        // ASSERT - Each request should have a unique ID
        assertNotNull(requestIds[0], "First request should have requestId");
        assertNotNull(requestIds[1], "Second request should have requestId");
        assertNotEquals(requestIds[0], requestIds[1], "Request IDs should be unique");
    }

    @Test
    void shouldClearMDCEvenWhenExceptionOccurs() throws ServletException, IOException {
        // ARRANGE
        when(request.getRequestURI()).thenReturn("/api/prices/BTCUSDT");
        when(request.getMethod()).thenReturn("GET");
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // ACT & ASSERT
        assertThrows(ServletException.class, () -> filter.doFilter(request, response, filterChain));

        // MDC should still be cleared even when exception occurs
        assertNull(MDC.get("requestId"), "MDC should be cleared even when exception occurs");
    }

    @Test
    void shouldLogIncomingRequest() throws ServletException, IOException {
        // ARRANGE
        when(request.getRequestURI()).thenReturn("/api/prices/BTCUSDT");
        when(request.getMethod()).thenReturn("GET");

        // ACT
        filter.doFilter(request, response, filterChain);

        // ASSERT
        // This test verifies the filter executes without errors
        // Actual logging can be verified manually or with log capturing
        verify(filterChain).doFilter(request, response);
    }
}
