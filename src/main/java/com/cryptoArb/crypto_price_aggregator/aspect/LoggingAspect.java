package com.cryptoArb.crypto_price_aggregator.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Logging Aspect for Service Layer
 *
 * Phase 8 - Observability:
 * - Logs method entry, exit, and execution time
 * - Logs exceptions thrown by methods
 * - Uses AOP to separate cross-cutting concerns from business logic
 *
 * Why AOP: Aspect-Oriented Programming enables clean separation of
 * logging concerns from business logic (SOLID principles)
 *
 * Design Decisions:
 * - @Around advice allows logging before, after, and around method execution
 * - Pointcut targets all service layer methods for comprehensive coverage
 * - Logs method signature and arguments for debugging
 * - Measures execution time for performance monitoring
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * Around advice for all service layer methods
     * Pointcut: execution(* com.cryptoArb.crypto_price_aggregator.service..*(..))
     * Matches: All methods in service package and sub-packages
     */
    @Around("execution(* com.cryptoArb.crypto_price_aggregator.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        // Log method entry
        log.debug("Entering method: {} with args: {}", methodName, Arrays.toString(args));

        try {
            // Proceed with method execution
            Object result = joinPoint.proceed();

            // Log method exit
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Exiting method: {} with result: {} (execution time: {}ms)",
                    methodName, result, executionTime);

            return result;

        } catch (Throwable ex) {
            // Log exception
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Exception in method: {} (execution time: {}ms): {}",
                    methodName, executionTime, ex.getMessage());
            throw ex; // Re-throw exception to maintain normal flow
        }
    }
}
