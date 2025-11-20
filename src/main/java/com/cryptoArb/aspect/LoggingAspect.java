package com.cryptoArb.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging execution time of service methods.
 *
 * This class uses Spring AOP to intercept method calls to beans in the
 * service layer. It wraps the execution in a timer and logs the duration.
 */
@Aspect // Aspect annotation is used to define an aspect; which is a class that implements cross-cutting concerns ; meaning it implements functionality that is common to many classes and is not specific to a single class
@Component // Component annotation is used to define a Spring bean; which is a class that is managed by Spring and can be injected into other classes
public class LoggingAspect {

    // Logger is used to log messages to the console
    // LoggerFactory is used to create a logger instance
    // we can see the logs in the console for example when we run the application like
    // Execution time for com.cryptoArb.service.PriceService.getConsolidatedPriceForPair :: 10 ms
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Defines the selection criteria for service methods.
     * * Pointcut breakdown:
     * - execution(* ...): Matches method execution.
     * - com.cryptoArb.service..: Matches service package and sub-packages.
     * - *(..): Matches any method with any arguments.
     */
    @Pointcut("execution(* com.cryptoArb.service..*(..))")
    public void serviceMethods() {
        // Pointcut signature method - must be empty
    }

    /**
     * Intercepts execution of the defined service methods.
     */
    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Get the method signature for logging clarity
        String className = joinPoint.getSignature().getDeclaringTypeName(); // e.g. className could be com.cryptoArb.service.PriceService
        String methodName = joinPoint.getSignature().getName(); // e.g. methodName could be getConsolidatedPriceForPair

        try {
            return joinPoint.proceed();
        } finally {
            // This block runs whether the method succeeded or failed
            long duration = System.currentTimeMillis() - startTime;

            log.info("Execution time for {}.{} :: {} ms",
                    className, methodName, duration);
        }
    }
}