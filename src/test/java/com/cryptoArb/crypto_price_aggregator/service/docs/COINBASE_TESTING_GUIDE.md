# Coinbase Testing Guide

This guide explains how to test the Coinbase price fetcher in the crypto-price-aggregator project.

## Overview

The `CoinbaseFetcher` class fetches cryptocurrency prices from the Coinbase API. Testing involves:
1. **Unit Tests** - Testing with mocked HTTP responses
2. **Integration Tests** - Testing with real API calls (optional)
3. **Running Tests** - Using Maven or IDE

---

## 1. Unit Testing (Recommended)

### Current Test File
Location: `src/test/java/com/cryptoArb/crypto_price_aggregator/service/CoinbaseFetcherTest.java`

The existing test uses `MockRestServiceServer` to mock HTTP responses without making real API calls.

### How the Unit Test Works

```java
@Test
void testFetchPrice_Success() throws Exception {
    // 1. Create mock JSON response matching Coinbase API format
    String jsonResponse = "{\"trade_id\": 4729088, \"price\": \"333.99\", \"size\": \"0.193\", \"bid\": \"333.98\", \"ask\": \"333.99\", \"volume\": \"5957.11914015\", \"time\": \"2015-11-14T20:46:03.511254Z\"}";

    // 2. Set up mock server to expect a specific URL
    mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
            .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));

    // 3. Call the fetcher
    CurrencyPair pair = CurrencyPair.of("BTC", "USD");
    PriceTick tick = fetcher.fetchPrice(pair);

    // 4. Assert the results
    assertNotNull(tick);
    assertEquals(Exchange.COINBASE, tick.getExchange());
    assertEquals(new BigDecimal("333.98"), tick.getBid());
    assertEquals(new BigDecimal("333.99"), tick.getAsk());

    // 5. Verify the mock was called
    mockServer.verify();
}
```

### Key Components

- **MockRestServiceServer**: Intercepts HTTP calls and returns predefined responses
- **CurrencyPair**: Represents a trading pair (e.g., BTC-USD)
- **PriceTick**: Contains the fetched price data (bid, ask, exchange, timestamp)
- **ObjectMapper**: Parses JSON responses

---

## 2. Running Tests

### Option A: Run All Tests
```bash
mvn test
```

### Option B: Run Only Coinbase Tests
```bash
mvn test -Dtest=CoinbaseFetcherTest
```

### Option C: Run from IDE
- Right-click on `CoinbaseFetcherTest.java` → Run
- Or use keyboard shortcut (typically Ctrl+Shift+F10 in IntelliJ)

---

## 3. Understanding the Coinbase API

### API Endpoint
```
GET https://api.exchange.coinbase.com/products/{SYMBOL}/ticker
```

### Symbol Format
- Coinbase uses hyphen-separated pairs: `BTC-USD`, `ETH-USD`, `DOGE-USD`
- The fetcher automatically converts `CurrencyPair("BTC", "USD")` to `BTC-USD`

### Response Format
```json
{
  "trade_id": 4729088,
  "price": "333.99",
  "size": "0.193",
  "bid": "333.98",
  "ask": "333.99",
  "volume": "5957.11914015",
  "time": "2015-11-14T20:46:03.511254Z"
}
```

### Key Fields Used
- **bid**: Best buy price (what buyers are willing to pay)
- **ask**: Best sell price (what sellers are asking)

---

## 4. Adding More Test Cases

### Test Case: Different Currency Pair
```java
@Test
void testFetchPrice_ETH_USD() throws Exception {
    String jsonResponse = "{\"bid\": \"2000.50\", \"ask\": \"2000.75\"}";
    
    mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/ETH-USD/ticker"))
            .andRespond(withSuccess(jsonResponse, APPLICATION_JSON));
    
    CurrencyPair pair = CurrencyPair.of("ETH", "USD");
    PriceTick tick = fetcher.fetchPrice(pair);
    
    assertEquals(new BigDecimal("2000.50"), tick.getBid());
    assertEquals(new BigDecimal("2000.75"), tick.getAsk());
    mockServer.verify();
}
```

### Test Case: Error Handling
```java
@Test
void testFetchPrice_ApiError() throws Exception {
    mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
            .andRespond(withServerError());
    
    CurrencyPair pair = CurrencyPair.of("BTC", "USD");
    
    assertThrows(PriceFetchException.class, () -> fetcher.fetchPrice(pair));
    mockServer.verify();
}
```

### Test Case: Invalid JSON Response
```java
@Test
void testFetchPrice_InvalidJson() throws Exception {
    String invalidJson = "{\"invalid\": \"response\"}";
    
    mockServer.expect(requestTo("https://api.exchange.coinbase.com/products/BTC-USD/ticker"))
            .andRespond(withSuccess(invalidJson, APPLICATION_JSON));
    
    CurrencyPair pair = CurrencyPair.of("BTC", "USD");
    
    assertThrows(PriceFetchException.class, () -> fetcher.fetchPrice(pair));
    mockServer.verify();
}
```

---

## 5. Integration Testing (Optional - Real API Calls)

If you want to test against the real Coinbase API:

### Create Integration Test
Location: `src/test/java/com/cryptoArb/crypto_price_aggregator/service/CoinbaseIntegrationTest.java`

```java
@SpringBootTest
class CoinbaseIntegrationTest {

    @Autowired
    private CoinbaseFetcher fetcher;

    @Test
    void testFetchPrice_RealAPI() throws Exception {
        CurrencyPair pair = CurrencyPair.of("BTC", "USD");
        PriceTick tick = fetcher.fetchPrice(pair);

        assertNotNull(tick);
        assertNotNull(tick.getBid());
        assertNotNull(tick.getAsk());
        assertTrue(tick.getBid().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(tick.getAsk().compareTo(BigDecimal.ZERO) > 0);
    }
}
```

### Run Integration Test
```bash
mvn test -Dtest=CoinbaseIntegrationTest
```

**Note**: Integration tests are slower and depend on external services. Use unit tests for CI/CD pipelines.

---

## 6. Test Dependencies

The project uses these testing libraries (from `spring-boot-starter-test`):
- **JUnit 5**: Test framework
- **Mockito**: Mocking framework
- **Spring Test**: Spring-specific testing utilities
- **AssertJ**: Fluent assertions

---

## 7. Troubleshooting

### Issue: Test Fails with "Unexpected request"
**Cause**: The mock server expected a different URL or method.
**Solution**: Check the URL format and ensure the symbol is correct (e.g., `BTC-USD` not `BTC_USD`).

### Issue: Test Fails with "No response configured"
**Cause**: The mock server wasn't set up for the request.
**Solution**: Ensure `mockServer.expect()` is called before `fetcher.fetchPrice()`.

### Issue: Test Hangs
**Cause**: The fetcher has a 500ms artificial latency (`Thread.sleep(500)`).
**Solution**: This is intentional. Tests should complete within a few seconds.

### Issue: Real API Returns 429 (Rate Limited)
**Cause**: Too many requests to Coinbase API.
**Solution**: Use unit tests with mocks instead of integration tests.

---

## 8. Best Practices

1. **Use Unit Tests by Default**: Mock external APIs to avoid dependencies
2. **Test Edge Cases**: Empty responses, malformed JSON, network errors
3. **Verify Mock Calls**: Always call `mockServer.verify()` to ensure expectations were met
4. **Use Descriptive Names**: Test method names should describe what they test
5. **Keep Tests Fast**: Avoid real API calls in unit tests
6. **Test Error Handling**: Ensure exceptions are properly thrown and caught

---

## 9. Quick Reference

| Task | Command |
|------|---------|
| Run all tests | `mvn test` |
| Run Coinbase tests only | `mvn test -Dtest=CoinbaseFetcherTest` |
| Run with verbose output | `mvn test -X` |
| Skip tests during build | `mvn clean install -DskipTests` |
| Run single test method | `mvn test -Dtest=CoinbaseFetcherTest#testFetchPrice_Success` |

---

## 10. Related Files

- **Implementation**: `src/main/java/com/cryptoArb/crypto_price_aggregator/service/CoinbaseFetcher.java`
- **Test**: `src/test/java/com/cryptoArb/crypto_price_aggregator/service/CoinbaseFetcherTest.java`
- **Domain Models**: `src/main/java/com/cryptoArb/crypto_price_aggregator/domain/`
- **Exception**: `src/main/java/com/cryptoArb/crypto_price_aggregator/exception/PriceFetchException.java`
