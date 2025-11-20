package com.cryptoArb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Configuration
@ConfigurationProperties(prefix = "cpa")
public class CpaConfigProperties {

    /**
     * Frequency of fetching prices in milliseconds.
     */
    @Setter
    private long fetchIntervalMs = 5000;

    private final Exchanges exchanges = new Exchanges();


    @Setter
    @Getter
    public static class Exchanges {
        /**
         * List of enabled exchange IDs (e.g. coinbase, binance).
         */
        private List<String> enabled = new ArrayList<>();


    }
}