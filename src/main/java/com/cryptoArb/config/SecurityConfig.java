package com.cryptoArb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the Crypto Price Aggregator API.
 *  Refactored to include CORS configuration.
 * Architecture: OAuth2 Resource Server
 *
 * This application does NOT handle user authentication (no login flow).
 * Instead, it validates JWTs issued by an external Identity Provider.
 *
 * Flow:
 * 1. Client obtains JWT from external IdP (e.g., Okta, Keycloak, Auth0)
 * 2. Client includes JWT in Authorization header: "Bearer <token>"
 * 3. Spring Security intercepts request and validates JWT signature/claims
 * 4. If valid, request proceeds; if invalid, returns 401 Unauthorized
 *
 * Configuration:
 * - All /api/v1/** endpoints require authentication
 * - Actuator endpoints (if present) can be configured separately
 * - Stateless session management (no server-side sessions)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * DECISION: Use SecurityFilterChain bean (Spring Security 6.x approach)
     * WHY: This is the modern, recommended approach. The old WebSecurityConfigurerAdapter
     * pattern was deprecated in Spring Security 5.7 and removed in 6.0.
     *
     * @param http The HttpSecurity builder to configure
     * @return The configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RequestLoggingFilter requestLoggingFilter) throws Exception {
        http
                // 1. Insert our custom logging filter
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)

                // 2. Configure CORS (Cross-Origin Resource Sharing)
                // This tells Spring Security to look for a bean named 'corsConfigurationSource'
                .cors(Customizer.withDefaults())

                // 3. Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Require authentication for all /api/v1/** endpoints
                        .requestMatchers("/api/v1/**").authenticated()
                        // --- DEV TESTING MODIFICATION START ---
                        // TEMPORARY: Allow public access to API for local dev testing without an Auth Server
//                        .requestMatchers("/api/v1/**").permitAll()
                        // --- DEV TESTING MODIFICATION END ---

                        // --- MODIFIED SECTION START ---
                        // Allow public access to Health AND Metrics for verification
                        .requestMatchers("/actuator/health", "/actuator/metrics/**", "/actuator/prometheus").permitAll()
                        // --- MODIFIED SECTION END ---

                        // Optional: Allow public access to Swagger UI
                        // (Useful for API documentation)
                        // http://localhost:8080/v3/api-docs
                        //  http://localhost:8080/swagger-ui/index.html
                        //    http://localhost:8080/swagger-ui.html
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()


                        // Optional: Secure other actuator endpoints
                        .requestMatchers("/actuator/**").authenticated()

                        // Deny all other requests by default
                        .anyRequest().denyAll()
                )

                // 4. Configure OAuth2 Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )

                // 5. Stateless session management
                .sessionManagement(session -> session
                        // STATELESS: No server-side sessions
                        // Each request must include a valid JWT
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Disable CSRF protection
                // WHY: Not needed for stateless JWT APIs
                // CSRF protection is designed for browser-based, session-based apps
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Defines the CORS configuration rules.
     * This bean is automatically detected by the .cors() method in the security chain.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow the frontend dev server (matching our test)
        // In the Refactor phase, we will move this to application.properties
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow standard headers (Authorization is crucial for JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID"));

        // Register this config for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}