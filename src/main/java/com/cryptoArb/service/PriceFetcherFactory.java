package com.cryptoArb.service;

import com.cryptoArb.fetcher.BinanceFetcher;
import com.cryptoArb.fetcher.CoinbaseFetcher;
import com.cryptoArb.fetcher.PriceFetcher;

// This is our "Factory"
public class PriceFetcherFactory {

    public PriceFetcher createFetcher(String exchangeId) {
        if ("coinbase".equalsIgnoreCase(exchangeId)) {
            return new CoinbaseFetcher();
        } else if ("binance".equalsIgnoreCase(exchangeId)) {
            return new BinanceFetcher();
        }
        return null; // Our current test doesn't check for this case yet
    }
}