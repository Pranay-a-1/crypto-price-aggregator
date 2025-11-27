package com.cryptoArb.scheduler;

import com.cryptoArb.service.PriceMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to trigger the price fetching and publishing process.
 */
@Component
@EnableScheduling
public class PriceFetchScheduler {

    private final PriceMessageProducer priceMessageProducer;

    @Autowired
    public PriceFetchScheduler(PriceMessageProducer priceMessageProducer) {
        this.priceMessageProducer = priceMessageProducer;
    }

    /**
     * Runs every 5 seconds (5000 ms).
     * The initial delay ensures the application has fully started before the first
     * run.
     */
    @Scheduled(fixedRateString = "${cpa.fetch-interval-ms:30000}", initialDelay = 5000)
    public void scheduleFetch() {
        priceMessageProducer.fetchAndPublish();
    }
}
