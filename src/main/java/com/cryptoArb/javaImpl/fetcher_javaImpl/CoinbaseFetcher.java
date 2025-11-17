package com.cryptoArb.javaImpl.fetcher_javaImpl;

import com.cryptoArb.exception.PriceFetchException;
import com.cryptoArb.javaImpl.domain_records.PriceTick;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CoinbaseFetcher implements PriceFetcher {

    @Override
    public List<PriceTick> fetchPrices() throws PriceFetchException {
        // Minimal implementation
        System.out.println("Fetching from Coinbase...");

        // --- NEW ---
        // Simulate network I/O delay as per Phase 6
        try {
            TimeUnit.MILLISECONDS.sleep(50); // Simulate 50ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceFetchException("Coinbase fetch interrupted", e);
        }
        // --- END NEW ---

        return Collections.emptyList();
    }
}