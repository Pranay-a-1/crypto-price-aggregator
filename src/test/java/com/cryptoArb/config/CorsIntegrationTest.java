package com.cryptoArb.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test to verify CORS configuration.
 * * We simulate a "Pre-flight" OPTIONS request, which browsers send automatically
 * before making a cross-origin request (like POST or GET with custom headers).
 */
@SpringBootTest(properties = {
        // Use dummy JWK URI to bypass startup checks
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/oauth2/jwks"
})
@AutoConfigureMockMvc
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow cross-origin requests from allowed origins")
    void shouldReturnCorsHeadersForValidOrigin() throws Exception {
        // --- Given ---
        String origin = "http://localhost:3000"; // A typical React/Vue dev server

        // --- When ---
        // We simulate an OPTIONS pre-flight request to a secured endpoint
        mockMvc.perform(options("/api/v1/price/BTC-USD")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "GET"))

                // --- Then ---
                // 1. Should return 200 OK (Pre-flight successful)
                .andExpect(status().isOk())

                // 2. Should echo back the allowed origin
                .andExpect(header().string("Access-Control-Allow-Origin", origin))

                // 3. Should allow the methods we need
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"));
    }

    @Test
    @DisplayName("Should deny or ignore cross-origin requests from disallowed origins")
    void shouldNotReturnCorsHeadersForInvalidOrigin() throws Exception {
        // --- Given ---
        String origin = "http://evil-site.com";

        // --- When ---
        mockMvc.perform(options("/api/v1/price/BTC-USD")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "GET"))

                // --- Then ---
                // If configured correctly for specific origins, this should typically
                // return 403 Forbidden or simply NOT include the CORS headers.
                // For this test, verifying the absence of the Allow-Origin header is sufficient proof.
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}