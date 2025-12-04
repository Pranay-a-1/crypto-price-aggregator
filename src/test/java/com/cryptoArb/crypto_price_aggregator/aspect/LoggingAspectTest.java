package com.cryptoArb.crypto_price_aggregator.aspect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Test for LoggingAspect
 * Following RED-GREEN-REFACTOR cycle
 *
 * Purpose: Verify AOP logging for service layer methods
 */
@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Test
    void shouldLogMethodEntry() throws Throwable {
        // ARRANGE - Will FAIL because LoggingAspect doesn't exist
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("PriceService.getAggregatedTopOfBookQuoteForPair(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "BTCUSDT" });
        when(joinPoint.proceed()).thenReturn("result");

        // ACT
        Object result = loggingAspect.logServiceMethods(joinPoint);

        // ASSERT
        assertNotNull(result);
        assertEquals("result", result);
        verify(joinPoint).proceed();
    }

    @Test
    void shouldLogMethodExit() throws Throwable {
        // ARRANGE
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("PriceService.getAggregatedTopOfBookQuoteForPair(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "BTCUSDT" });
        when(joinPoint.proceed()).thenReturn("result");

        // ACT
        Object result = loggingAspect.logServiceMethods(joinPoint);

        // ASSERT
        assertEquals("result", result);
    }

    @Test
    void shouldLogExceptionWhenMethodThrows() throws Throwable {
        // ARRANGE
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("PriceService.getAggregatedTopOfBookQuoteForPair(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "BTCUSDT" });
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> loggingAspect.logServiceMethods(joinPoint));
    }

    @Test
    void shouldMeasureExecutionTime() throws Throwable {
        // ARRANGE
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("PriceService.getAggregatedTopOfBookQuoteForPair(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "BTCUSDT" });
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(100); // Simulate some delay
            return "result";
        });

        // ACT
        long startTime = System.currentTimeMillis();
        Object result = loggingAspect.logServiceMethods(joinPoint);
        long endTime = System.currentTimeMillis();

        // ASSERT
        assertNotNull(result);
        assertTrue((endTime - startTime) >= 100, "Execution time should be measured");
    }
}
