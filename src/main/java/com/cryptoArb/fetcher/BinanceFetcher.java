package com.cryptoArb.fetcher;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.exception.PriceFetchException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BinanceFetcher implements PriceFetcher {

    @Override
    public List<PriceTick> fetchPrices() throws PriceFetchException {
        // Minimal implementation
        System.out.println("Fetching from Binance...");

        // --- NEW ---
        // Simulate network I/O delay as per Phase 6
        try {
            TimeUnit.MILLISECONDS.sleep(70); // Simulate 70ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceFetchException("Binance fetch interrupted", e);
        }
        // --- END NEW ---

        return Collections.emptyList();
    }
}