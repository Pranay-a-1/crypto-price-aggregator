package com.cryptoArb.controller;

import com.cryptoArb.application.controller.AsyncPriceController;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.domain_spring.ReportResult;
import com.cryptoArb.service.PriceReportService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Phase 14: Async API Controller Test.
 * Tests for AsyncPriceController demonstrating DeferredResult behavior.
 */
@ExtendWith(MockitoExtension.class)
class AsyncPriceControllerTest {

    private AsyncPriceController asyncPriceController;

    @Mock
    private PriceReportService priceReportService;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        asyncPriceController = new AsyncPriceController(priceReportService, meterRegistry);

        // Set timeout to 2 seconds for faster tests
        ReflectionTestUtils.setField(asyncPriceController, "timeoutSeconds", 2L);
    }

    @Test
    @DisplayName("Should start async report and return HTTP 202 with requestId")
    void testStartAsyncReport() {
        // Given
        ReportResult mockResult = ReportResult.completed("PRICE_SUMMARY", 10L, new BigDecimal("50000.00"));
        CompletableFuture<ReportResult> futureMock = CompletableFuture.completedFuture(mockResult);
        when(priceReportService.generateReport(any(CurrencyPair.class))).thenReturn(futureMock);

        // When
        ResponseEntity<Map<String, Object>> response = asyncPriceController.startAsyncReport("BTC-USD");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKeys("requestId", "status", "message", "checkUrl");
        assertThat(body.get("status")).isEqualTo("PROCESSING");
    }

    @Test
    @DisplayName("Should reject invalid currency pair format")
    void testStartAsyncReportWithInvalidPair() {
        // When
        ResponseEntity<Map<String, Object>> response = asyncPriceController.startAsyncReport("INVALID");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return completed result immediately if ready")
    void testGetAsyncResultWhenComplete() throws Exception {
        // Given
        ReportResult mockResult = ReportResult.completed("PRICE_SUMMARY", 5L, new BigDecimal("50100.00"));
        CompletableFuture<ReportResult> completedFuture = CompletableFuture.completedFuture(mockResult);
        when(priceReportService.generateReport(any(CurrencyPair.class))).thenReturn(completedFuture);

        ResponseEntity<Map<String, Object>> startResponse = asyncPriceController.startAsyncReport("ETH-USD");
        String requestId = (String) startResponse.getBody().get("requestId");

        // When
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = asyncPriceController
                .getAsyncResult(requestId);

        // Then
        await().atMost(1, TimeUnit.SECONDS)
                .until(() -> deferredResult.getResult() != null);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> resultResponse = (ResponseEntity<Map<String, Object>>) deferredResult
                .getResult();
        assertThat(resultResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> body = resultResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should return 404 for unknown requestId")
    void testGetAsyncResultWithUnknownRequestId() {

        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = asyncPriceController
                .getAsyncResult("unknown-request-id");

        // Then
        await().atMost(1, TimeUnit.SECONDS)
                .until(() -> deferredResult.getResult() != null);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = 
                (ResponseEntity<Map<String, Object>>) deferredResult.getResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
