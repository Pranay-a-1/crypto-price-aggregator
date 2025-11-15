package com.cryptoArb.fetcher;

import com.cryptoArb.domain_records.PriceTick;
import com.cryptoArb.exception.PriceFetchException;

import java.util.List;

// This is the "Strategy" interface
public interface PriceFetcher {

    // The test doesn't check this method, but the interface needs
    // a purpose. We'll implement it properly in a later cycle.
    List<PriceTick> fetchPrices() throws PriceFetchException;


    /**
     * A default method to provide the exchange's name.
     * This fulfills the Phase 2 requirement.
     * @return The simple class name, minus the 'Fetcher' suffix.
     */
    default String getExchangeName() {
        return this.getClass().getSimpleName().replace("Fetcher", "");
    }


}