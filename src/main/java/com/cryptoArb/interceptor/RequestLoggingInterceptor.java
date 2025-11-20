package com.cryptoArb.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * Interceptor for logging HTTP requests and adding tracking headers.
 * Phase 12, Task 3: Filters & Interceptors.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * Intercepts the request before it reaches the controller.
     * Logs the incoming request method and URI.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Log the incoming request
        // can be GET, POST, PUT, DELETE, etc.
        // can see the request URI in the logs available in the console
        log.info("Incoming Request: {} {}", request.getMethod(), request.getRequestURI());

        // We return true to allow the request to proceed to the controller
        return true;
    }

    /**
     * Executed after the controller method but before the view is rendered.
     * Adds a unique X-Request-ID header to the response for tracing.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Generate a unique ID for this request
        //e.g. requestId could be 123e4567-e89b-12d3-a456-426614174000
        String requestId = UUID.randomUUID().toString();

        // Add it to the response headers
        //e.g. X-Request-ID: 123e4567-e89b-12d3-a456-426614174000
        response.addHeader(X_REQUEST_ID_HEADER, requestId);
    }
}