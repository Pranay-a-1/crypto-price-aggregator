package com.cryptoArb.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CpaConfigPropertiesTest.TestConfig.class)
@TestPropertySource(properties = {
        "cpa.exchanges.enabled[0]=coinbase",
        "cpa.exchanges.enabled[1]=binance",
        "cpa.fetch-interval-ms=2000"
})
class CpaConfigPropertiesTest {

    @Autowired
    private CpaConfigProperties properties;

    @Test
    void shouldBindProperties() {
        assertThat(properties.getExchanges().getEnabled()).containsExactly("coinbase", "binance");
        assertThat(properties.getFetchIntervalMs()).isEqualTo(2000L);
    }

    // Minimal config to enable property loading for the test
    @EnableConfigurationProperties(CpaConfigProperties.class)
    static class TestConfig {}
}