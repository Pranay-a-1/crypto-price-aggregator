package com.cryptoArb.integration;

import com.cryptoArb.application.service.ExchangeInfoService;
import com.cryptoArb.domain_spring.ExchangeInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.cryptoArb.CryptoPriceAggregatorApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(classes = CryptoPriceAggregatorApplication.class, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
public class L2CacheTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("exchangeInfo");
        }
    }

    @Autowired
    private ExchangeInfoService exchangeInfoService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testL2Caching() {
        // 1. Create and save an entity
        ExchangeInfo info = new ExchangeInfo("test-exchange", "Test Exchange", "http://test.com", 0.1);
        exchangeInfoService.saveExchangeInfo(info);

        // 2. Clear cache to ensure we start fresh
        var cache = Objects.requireNonNull(cacheManager.getCache("exchangeInfo"),
                "Cache 'exchangeInfo' should be configured");
        cache.clear();

        // 3. First fetch - should hit DB and populate cache
        Optional<ExchangeInfo> fetched1 = exchangeInfoService.getExchangeInfo("test-exchange");
        assertThat(fetched1).isPresent();
        assertThat(fetched1.get().getFullName()).isEqualTo("Test Exchange");

        // 4. Second fetch - should hit cache
        Optional<ExchangeInfo> fetched2 = exchangeInfoService.getExchangeInfo("test-exchange");
        assertThat(fetched2).isPresent();
        assertThat(fetched2.get()).isEqualTo(fetched1.get());

        // Verify cache contains the item
        assertThat(cache).isNotNull();
        assertThat(cache.get("test-exchange")).isNotNull();
    }
}
