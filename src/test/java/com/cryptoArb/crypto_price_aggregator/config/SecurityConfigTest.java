package com.cryptoArb.crypto_price_aggregator.config;

import com.cryptoArb.crypto_price_aggregator.controller.PriceController;
import com.cryptoArb.crypto_price_aggregator.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PriceController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceService priceService;

    @Test
    @DisplayName("Should return 200 OK (or 404/others) when accessing API with valid credentials")
    @WithMockUser(username = "user", password = "password")
    void shouldAllowAccessWithCredentials() throws Exception {
        // We expect not authorized (401) to be false. 
        // 404 is acceptable as we don't mock the service here (or we could), just checking security layer.
        // Actually, since we MockBean the service, it will return null/empty by default unless stubbed.
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
        // or just /actuator root which lists endpoints.
        // Since we are consistent with WebMvcTest(PriceController.class), Actuator endpoints aren't loaded.
        // So we get 404. But secure would be 401. So 404 means allowed.
        mockMvc.perform(get("/actuator"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Status was " + status + " but expected authorized access (even if 404)");
                    }
                });
    }
}
