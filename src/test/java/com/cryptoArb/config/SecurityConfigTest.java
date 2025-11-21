package com.cryptoArb.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Spring Security configuration.
 *
 * Tests that:
 * 1. All endpoints require authentication
 * 2. Valid JWT tokens are accepted
 * 3. Invalid/missing tokens are rejected with 401
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Configuration Integration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 401 when accessing /api/v1/price without token")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        // When: We try to access a secured endpoint without a token
        mockMvc.perform(get("/api/v1/price/BTC-USD"))
                // Then: We should get 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when accessing /api/v1/arbitrage without token")
    void shouldReturn401ForArbitrageEndpointWithoutToken() throws Exception {
        // When: We try to access the arbitrage endpoint without a token
        mockMvc.perform(get("/api/v1/arbitrage"))
                // Then: We should get 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when providing invalid Bearer token")
    void shouldReturn401WithInvalidToken() throws Exception {
        // Given: An invalid JWT token
        String invalidToken = "Bearer invalid.jwt.token";

        // When: We try to access a secured endpoint with an invalid token
        mockMvc.perform(get("/api/v1/price/BTC-USD")
                        .header("Authorization", invalidToken))
                // Then: We should get 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when providing malformed Authorization header")
    void shouldReturn401WithMalformedAuthHeader() throws Exception {
        // Given: A malformed Authorization header (missing "Bearer" prefix)
        String malformedHeader = "not-a-bearer-token";

        // When: We try to access a secured endpoint
        mockMvc.perform(get("/api/v1/price/BTC-USD")
                        .header("Authorization", malformedHeader))
                // Then: We should get 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }

    // Note: Testing with a *valid* JWT would require either:
    // 1. A real OAuth2 Authorization Server (complex setup)
    // 2. A mock JWT with a signing key we control
    // 3. Using @WithMockUser (but that bypasses JWT validation)
    //
    // For this phase, we focus on proving the security *layer* exists
    // and rejects unauthorized requests. Token validation itself is
    // handled by Spring Security's built-in JWT decoder.
}