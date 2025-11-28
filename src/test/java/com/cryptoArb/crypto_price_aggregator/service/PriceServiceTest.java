package com.cryptoArb.crypto_price_aggregator.service;

// import com.cryptoArb.crypto_price_aggregator.domain.ConsolidatedPrice;
// import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;

// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.assertTrue;

// class PriceServiceTest {

// @Test
// @DisplayName("Given unknown pair, when getConsolidatedPrice, then returns
// empty")
// void givenUnknownPair_whenGetConsolidatedPrice_thenReturnsEmpty() {
// // Arrange
// // We are programming to an interface 'PriceService' which we haven't created
// // yet.
// // We assume a concrete implementation 'PriceServiceImpl' will exist.
// PriceService priceService = new PriceServiceImpl();
// CurrencyPair unknownPair = new CurrencyPair("UNKNOWN", "COIN");

// // Act
// Optional<ConsolidatedPrice> result =
// priceService.getConsolidatedPrice(unknownPair);

// // Assert
// assertTrue(result.isEmpty(), "Service should return empty Optional for
// unknown currency pairs");
// }
// }