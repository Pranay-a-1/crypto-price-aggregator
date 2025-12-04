package com.cryptoArb.crypto_price_aggregator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 401 Unauthorized when accessing API without credentials")
    void shouldReturn401WithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/prices/BTC/USD"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 200 OK (or 404/others) when accessing API with valid credentials")
    @WithMockUser(username = "user", password = "password")
    void shouldAllowAccessWithCredentials() throws Exception {
        // We expect not authorized (401) to be false. 
        // 404 is acceptable as we don't mock the service here, just checking security layer.
        // Actually, since we are in SpringBootTest, the service might throw or return 404 if DB empty.
        // But the key is IT IS NOT 401/403.
        mockMvc.perform(get("/api/prices/BTC/USD"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Status was " + status + " but expected authorized access");
                    }
                });
    }

    @Test
    @DisplayName("Should allow public access to actuator info")
    void shouldAllowPublicAccessToActuator() throws Exception {
        // Checking /actuator/info which should be 200 OK regardless of health status
        // or just /actuator root which lists endpoints
        mockMvc.perform(get("/actuator"))
                .andExpect(status().isOk());
    }
}
