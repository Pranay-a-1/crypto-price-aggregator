package com.cryptoArb.crypto_price_aggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for application beans.
 * Defines RestTemplate bean for HTTP calls.
 * Configures static resource serving for frontend.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configure static resource handlers to serve frontend files.
     * Frontend files are served from the 'frontend' directory at the project root.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("file:frontend/")
                .setCachePeriod(0); // No caching for development
    }
}
