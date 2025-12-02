# Phase 5 Walkthrough: Internal Decoupling with Application Events

## Goal
The goal of Phase 5 was to decouple the **fetching** of prices from the **processing/persistence** of those prices. In the previous monolithic design, `PriceServiceImpl` was responsible for triggering the fetch, waiting for results, saving them to the database, and then querying them.

By using Spring's Application Events (Observer Pattern), we separate these concerns:
1.  **Fetchers** are responsible only for fetching data and notifying the system ("I found a price!").
2.  **Listeners (Consumers)** are responsible for acting on that data (e.g., saving to the DB).
3.  **Service** is responsible for coordination and aggregation, but not the side effects of saving.

## Changes

### 1. Define the Event
We created a custom event `PriceTickFetchedEvent` that extends Spring's `ApplicationEvent`. This event carries the payload (the `PriceTick`).

**File:** `src/main/java/com/cryptoArb/crypto_price_aggregator/event/PriceTickFetchedEvent.java`

```java
public class PriceTickFetchedEvent extends ApplicationEvent {
    private final PriceTick priceTick;
    // constructor and getter...
}
```

### 2. Create the Consumer
We created `PriceTickConsumer` to listen for these events. This component now owns the responsibility of persistence.

**File:** `src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceTickConsumer.java`

```java
@Component
public class PriceTickConsumer {
    private final PriceTickRepository priceTickRepository;

    @EventListener
    public void handlePriceTickFetchedEvent(PriceTickFetchedEvent event) {
        priceTickRepository.save(event.getPriceTick());
    }
}
```

### 3. Refactor Fetchers (Publishers)
We updated all `PriceFetcher` implementations (`BinanceFetcher`, `CoinbaseFetcher`, `KrakenFetcher`, `MockPriceFetcher`) to inject `ApplicationEventPublisher`. Instead of just returning the tick, they now publish it as an event.

**Example (BinanceFetcher):**
```java
@Autowired
public BinanceFetcher(..., ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
}

@Override
public PriceTick fetchPrice(CurrencyPair pair) {
    // ... fetching logic ...
    PriceTick tick = new PriceTick(...);
    eventPublisher.publishEvent(new PriceTickFetchedEvent(this, tick)); // Fire event
    return tick;
}
```

### 4. Refactor PriceService
We removed the explicit `repository.saveAll(...)` call from `PriceServiceImpl`. The service triggers the engine, which triggers fetchers. The fetchers fire events, which saves the data (synchronously by default in Spring). The service then queries the repository as before.

**File:** `src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/PriceServiceImpl.java`

```java
// BEFORE:
// List<PriceTick> freshTicks = executionEngine.fetchPrices(fetchers, pair);
// repository.saveAll(freshTicks);

// AFTER:
executionEngine.fetchPrices(fetchers, pair); // Fetchers trigger events -> Consumer saves
// Then query repository...
```

## Testing
We updated unit tests to reflect this change:
- **Fetcher Tests:** Verifying that `publishEvent` is called.
- **Service Tests:** Mocking the repository to simulate that data was saved (since the consumer isn't present in a mocked unit test).
- **Integration Tests:** Verifying the full flow (Fetch -> Event -> Save -> Query) works with the real H2 database.
