package com.cryptoArb.controller;

import com.cryptoArb.domain_spring.ConsolidatedPrice;
import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.service.ArbitrageService;
import com.cryptoArb.service.PriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(PriceGraphQLController.class)
class PriceGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private PriceService priceService;

    @MockBean
    private ArbitrageService arbitrageService;

    @Test
    void shouldGetPrice() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        ConsolidatedPrice price = new ConsolidatedPrice(
                pair,
                Instant.now(),
                new BigDecimal("50000.00"),
                new com.cryptoArb.domain_spring.Exchange("coinbase"),
                new BigDecimal("50100.00"),
                new com.cryptoArb.domain_spring.Exchange("binance"));

        when(priceService.getConsolidatedPriceForPair(any(CurrencyPair.class))).thenReturn(Optional.of(price));

        String document = """
                query {
                    getPrice(pair: "BTC-USD") {
                        pair {
                            base
                            quote
                        }
                        bestBid
                        bestAsk
                    }
                }
                """;

        graphQlTester.document(document)
                .execute()
                .path("getPrice.pair.base").entity(String.class).isEqualTo("BTC")
                .path("getPrice.bestBid").entity(BigDecimal.class)
                .matches(val -> val.compareTo(new BigDecimal("50000.00")) == 0);
    }

    @Test
    void shouldGetArbitrageOpportunities() {
        CurrencyPair pair = new CurrencyPair("BTC", "USD");
        com.cryptoArb.domain_spring.ArbitrageOpportunity opportunity = new com.cryptoArb.domain_spring.ArbitrageOpportunity(
                pair,
                Instant.now(),
                new com.cryptoArb.domain_spring.Exchange("coinbase"),
                new BigDecimal("50000.00"),
                new com.cryptoArb.domain_spring.Exchange("binance"),
                new BigDecimal("50100.00"),
                new BigDecimal("0.20"));

        when(arbitrageService.getRecentOpportunities()).thenReturn(java.util.List.of(opportunity));

        String document = """
                query {
                    getArbitrageOpportunities {
                        pair {
                            base
                            quote
                        }
                        profitPercentage
                    }
                }
                """;

        graphQlTester.document(document)
                .execute()
                .path("getArbitrageOpportunities[0].pair.base").entity(String.class).isEqualTo("BTC")
                .path("getArbitrageOpportunities[0].profitPercentage").entity(BigDecimal.class)
                .matches(val -> val.compareTo(new BigDecimal("0.20")) == 0);
    }
}
