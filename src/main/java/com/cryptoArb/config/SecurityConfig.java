package com.cryptoArb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Crypto Price Aggregator API.
 *
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
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)  // this line is added to log the request and response ; requestLoggingFilter is a custom filter ; UsernamePasswordAuthenticationFilter is a default filter
                // 1. Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Require authentication for all /api/v1/** endpoints
                        .requestMatchers("/api/v1/**").authenticated()

                        // Optional: Allow public access to Actuator health endpoint
                        // (Useful for load balancers and monitoring tools)
                        .requestMatchers("/actuator/health").permitAll()

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

                // 2. Configure OAuth2 Resource Server with JWT validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        // Enable JWT authentication
                        .jwt(jwt -> {
                            // Additional JWT customization can go here
                            // For example: custom claim validation, authorities mapping
                            // For now, we use the defaults from application.properties
                        })
                )

                // 3. Configure session management
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
     * ALTERNATIVE CONFIGURATION (if you need more control):
     *
     * You can create a custom JwtDecoder bean to:
     * - Add custom claim validation
     * - Configure token caching
     * - Add custom error handling
     *
     * Example:
     *
     * @Bean
     * public JwtDecoder jwtDecoder() {
     *     // Create decoder from JWK Set URI
     *     NimbusJwtDecoder decoder = NimbusJwtDecoder
     *         .withJwkSetUri("https://your-idp.com/.well-known/jwks.json")
     *         .build();
     *
     *     // Add custom validators
     *     decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
     *         new JwtTimestampValidator(),
     *         new JwtIssuerValidator("https://your-idp.com"),
     *         new CustomClaimValidator()
     *     ));
     *
     *     return decoder;
     * }
     */
}