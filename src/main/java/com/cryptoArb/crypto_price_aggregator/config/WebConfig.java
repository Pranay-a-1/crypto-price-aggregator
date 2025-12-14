package com.cryptoArb.crypto_price_aggregator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for serving static frontend resources.
 * 
 * This configuration maps the /frontend/** URL path to serve static files
 * from the classpath:/static/frontend/ directory.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve frontend files from classpath:/static/frontend/
        registry
            .addResourceHandler("/frontend/**")
            .addResourceLocations("classpath:/static/frontend/");
        
        // Also serve the root path for convenience
        registry
            .addResourceHandler("/")
            .addResourceLocations("classpath:/static/frontend/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect root to frontend index
        registry.addRedirectViewController("/", "/frontend/index.html");
    }
}
