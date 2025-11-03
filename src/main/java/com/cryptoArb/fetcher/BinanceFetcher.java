package com.cryptoArb.fetcher;

import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;

import java.util.Collections;
import java.util.List;

public class BinanceFetcher implements PriceFetcher {

    @Override
    public List<PriceTick> fetchPrices() throws PriceFetchException {
        // Minimal implementation - just return an empty list for now.
        System.out.println("Fetching from Binance...");
        return Collections.emptyList();
    }
}