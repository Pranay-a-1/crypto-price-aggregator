package com.cryptoArb.crypto_price_aggregator.event;

import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a price tick is successfully fetched from an exchange.
 */
@Getter
public class PriceTickFetchedEvent extends ApplicationEvent {

    private final PriceTick priceTick;

    public PriceTickFetchedEvent(Object source, PriceTick priceTick) {
        super(source);
        this.priceTick = priceTick;
    }

}
