package com.cryptoArb.config;

import com.cryptoArb.interceptor.RequestLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Spring Web MVC settings.
 * Registers custom interceptors and CORS settings.
 * CORS is Cross-Origin Resource Sharing
 */
@Configuration
public class WebConfig implements WebMvcConfigurer { // implements WebMvcConfigurer to register custom interceptors and CORS settings

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Autowired
    public WebConfig(RequestLoggingInterceptor requestLoggingInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
    }

    /**
     * Registers the RequestLoggingInterceptor to apply to all paths.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**"); // Apply to all endpoints , we do this to intercept all requests
    }
}