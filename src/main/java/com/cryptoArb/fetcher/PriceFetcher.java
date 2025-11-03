package com.cryptoArb.fetcher;

import com.cryptoArb.domain.PriceTick;
import com.cryptoArb.exception.PriceFetchException;

import java.util.List;

// This is the "Strategy" interface
public interface PriceFetcher {

    // The test doesn't check this method, but the interface needs
    // a purpose. We'll implement it properly in a later cycle.
    List<PriceTick> fetchPrices() throws PriceFetchException;
}