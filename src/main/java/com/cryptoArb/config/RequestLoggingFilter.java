// src/main/java/com/cryptoArb/config/RequestLoggingFilter.java
package com.cryptoArb.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1) // Runs very early, before security decisions
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String X_REQUEST_ID = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        request.setAttribute(X_REQUEST_ID, requestId);
        response.setHeader(X_REQUEST_ID, requestId);

        log.info("==> Incoming Request: {} {} (Request-ID: {})",
                request.getMethod(), request.getRequestURI(), requestId);
//        System.out.println("==> Incoming Request: " + request.getMethod() + " " + request.getRequestURI() + " (Request-ID: " + requestId + ")");

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("<== Completed Request: {} {} {} (Request-ID: {})",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), requestId);
//            System.out.println("<== Completed Request: " + request.getMethod() + " " + request.getRequestURI() + " " + response.getStatus() + " (Request-ID: " + requestId + ")");
        }
    }
}