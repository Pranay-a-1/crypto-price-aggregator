package com.cryptoArb.application.controller;

import com.cryptoArb.domain_spring.PriceTick;
import com.cryptoArb.service.PriceStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/stream")
public class PriceStreamController {

    private final PriceStreamService priceStreamService;

    public PriceStreamController(PriceStreamService priceStreamService) {
        this.priceStreamService = priceStreamService;
    }

    @GetMapping(value = "/prices/{pair}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PriceTick> streamPrices(@PathVariable String pair) {
        return priceStreamService.getStream(pair);
    }
}
