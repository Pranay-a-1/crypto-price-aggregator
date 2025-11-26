package com.cryptoArb.service;

import com.cryptoArb.domain_spring.PriceTick;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class PriceStreamService {

    private final Sinks.Many<PriceTick> sink;

    public PriceStreamService() {
        // Multicast sink that replays the last 0 items (hot stream)
        // onBackpressureBuffer handles slow clients
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public void emit(PriceTick tick) {
        sink.tryEmitNext(tick);
    }

    public Flux<PriceTick> getStream(String pair) {
        return sink.asFlux()
                .filter(tick -> tick.getPair().toString().equalsIgnoreCase(pair) ||
                        (tick.getPair().getBase() + "-" + tick.getPair().getQuote()).equalsIgnoreCase(pair));
    }
}
