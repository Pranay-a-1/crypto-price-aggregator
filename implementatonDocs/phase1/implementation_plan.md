# Phase 1: Sequential Price Fetching with Mock Data

## Goal

Implement Phase 1 of the CryptoPriceAggregator following TDD principles with SOLID/DRY/KISS/YAGNI software design. Create a working REST endpoint that fetches and aggregates mock cryptocurrency prices sequentially from multiple mock exchanges.

## User Review Required

> [!IMPORTANT]
> **TDD Approach**: We will write tests FIRST (red), then implement code to make them pass (green), then refactor for quality. Each section below follows this cycle.

> [!IMPORTANT]
> **SOLID Principles Applied**:
> - **S**ingle Responsibility: Each class has one reason to change
> - **O**pen/Closed: Open for extension via interfaces
> - **L**iskov Substitution: Implementations are substitutable
> - **I**nterface Segregation: Small, focused interfaces
> - **D**ependency Inversion: Depend on abstractions (PriceFetcher interface)

> [!NOTE]
> **YAGNI**: We are NOT implementing database persistence, real HTTP calls, or async processing yet. These come in later phases.

## Proposed Changes

### Domain Layer

#### [NEW] [Exchange.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/Exchange.java)

**Purpose**: Enum representing different cryptocurrency exchanges

**Design Rationale**:
- **KISS**: Simple enum, no complex logic needed
- **DRY**: Single source of truth for exchange names
- **Type Safety**: Prevents typos like "BINANNCE"

**Test First** ([ExchangeTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/domain/ExchangeTest.java)):
- Verify all exchanges have non-empty names
- Test enum valueOf operations

#### [NEW] [PriceTick.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTick.java)

**Purpose**: Immutable domain model representing a single price quote from one exchange

**Design Rationale**:
- **Record**: Immutable by default (defensive programming)
- **BigDecimal**: Financial precision (no floating point errors)
- **Validation**: Fail-fast in canonical constructor
- **Single Responsibility**: Only represents price data, no business logic

**Test First** ([PriceTickTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTickTest.java)):
- Valid tick creation
- Null validation (pair, exchange, timestamp)
- Negative price validation
- Bid > Ask validation (market sanity check)

---

### Service Layer

#### [NEW] [PriceFetcher.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceFetcher.java)

**Purpose**: Interface for fetching prices from exchanges (Dependency Inversion Principle)

**Design Rationale**:
- **Interface Segregation**: Single method `fetchPrice(CurrencyPair pair)`
- **Open/Closed**: New fetchers can be added without modifying existing code
- **Testability**: Easy to mock in tests

#### [NEW] [MockPriceFetcher.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/MockPriceFetcher.java)

**Purpose**: Mock implementation generating random prices for Phase 1

**Design Rationale**:
- **YAGNI**: No real HTTP calls yet, just mock data
- **KISS**: Simple random number generation
- **Single Responsibility**: Only generates mock ticks
- **Configurable**: Constructor injection of Exchange enum

**Test First** ([MockPriceFetcherTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/service/MockPriceFetcherTest.java)):
- Returns non-null PriceTick
- Correct exchange assignment
- Bid < Ask invariant
- Timestamp is recent

#### [MODIFY] [PriceService.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceService.java)

**Changes**: Keep existing interface as-is (already well-designed)

#### [NEW] [PriceServiceImpl.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceServiceImpl.java)

**Purpose**: Implements price aggregation logic

**Design Rationale**:
- **Dependency Injection**: Constructor injection of List<PriceFetcher> (Dependency Inversion)
- **Sequential Fetching**: Simple for-loop (Phase 2 will add concurrency)
- **Aggregation Logic**: 
  - Best bid = MAX of all bids (highest price someone will pay)
  - Best ask = MIN of all asks (lowest price someone will sell)
- **Error Handling**: Gracefully skip failed fetchers, return empty if all fail
- **Single Responsibility**: Only aggregates, doesn't fetch or expose data

**Test Updates** ([PriceServiceTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/service/PriceServiceTest.java)):
- Existing test: Unknown pair returns empty ✅
- **New tests to add**:
  - Single fetcher returns correct consolidated price
  - Multiple fetchers aggregate correctly (max bid, min ask)
  - All fetchers fail → return empty
  - Some fetchers fail → aggregate from successful ones
  - Null currency pair → throws exception

---

### Controller Layer

#### [NEW] [PriceController.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/controller/PriceController.java)

**Purpose**: REST endpoint for fetching aggregated prices

**Design Rationale**:
- **Endpoint**: `GET /api/prices/{base}/{quote}` (RESTful design)
- **Dependency Injection**: Constructor injection of PriceService
- **Single Responsibility**: Only handles HTTP concerns, delegates to service
- **Error Handling**: Returns 404 if price not found, 500 for exceptions

**Test First** ([PriceControllerTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/controller/PriceControllerTest.java)):
- Valid request returns 200 with consolidated price
- Unknown pair returns 404
- Invalid pair (empty base/quote) returns 400
- Service exception returns 500

---

### Exception Handling

#### [NEW] [PriceFetchException.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/exception/PriceFetchException.java)

**Purpose**: Custom checked exception for price fetching errors

**Design Rationale**:
- **Explicit Error Handling**: Forces callers to handle failures
- **DRY**: Single exception type for all fetch errors
- **Extensible**: Can add error codes later

---

### Configuration

#### [MODIFY] [application.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application.properties)

**Changes**:
```properties
# Logging
logging.level.com.cryptoArb.crypto_price_aggregator=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Server
server.port=8080
```

## Verification Plan

### Automated Tests

**TDD Red-Green-Refactor Cycle**:

1. **Red Phase** (Write failing tests):
   ```bash
   # All tests should fail initially
   ./mvnw test
   ```

2. **Green Phase** (Implement minimal code):
   - Implement each class/method to make tests pass
   - No premature optimization

3. **Refactor Phase** (Clean up):
   - Apply SOLID principles
   - Remove duplication (DRY)
   - Simplify (KISS)
   - Remove unused code (YAGNI)

**Test Execution Order**:
1. Domain tests (ExchangeTest, PriceTickTest)
2. Service tests (MockPriceFetcherTest, PriceServiceTest)
3. Controller tests (PriceControllerTest)

**Final Verification**:
```bash
# All tests should pass
./mvnw clean test

# Build should succeed
./mvnw clean package
```

### Manual Verification

1. **Start the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Test REST endpoint**:
   ```bash
   # Should return aggregated mock prices
   curl http://localhost:8080/api/prices/BTC/USD | jq
   
   # Should return 404 for unknown pair
   curl -I http://localhost:8080/api/prices/UNKNOWN/COIN
   ```

3. **Verify logs**:
   - Check DEBUG logs show fetching from multiple exchanges
   - Verify aggregation logic (max bid, min ask)

## SOLID/DRY/KISS/YAGNI Compliance Checklist

- ✅ **Single Responsibility**: Each class has one reason to change
- ✅ **Open/Closed**: PriceFetcher interface allows new implementations
- ✅ **Liskov Substitution**: MockPriceFetcher is substitutable for PriceFetcher
- ✅ **Interface Segregation**: Small, focused interfaces (PriceFetcher, PriceService)
- ✅ **Dependency Inversion**: PriceServiceImpl depends on PriceFetcher abstraction
- ✅ **DRY**: No code duplication, single source of truth for domain models
- ✅ **KISS**: Simple, straightforward implementations without over-engineering
- ✅ **YAGNI**: No database, no real HTTP, no async - only what Phase 1 requires

## Implementation Order (TDD)

1. **Domain Layer** (30 min)
   - Write ExchangeTest → Implement Exchange
   - Write PriceTickTest → Implement PriceTick

2. **Service Layer** (60 min)
   - Write MockPriceFetcherTest → Implement MockPriceFetcher
   - Update PriceServiceTest with new scenarios → Implement PriceServiceImpl

3. **Controller Layer** (30 min)
   - Write PriceControllerTest → Implement PriceController

4. **Integration** (30 min)
   - Wire beans with @Service, @Component, @RestController
   - Manual testing with curl

5. **Refactor** (30 min)
   - Review for SOLID compliance
   - Remove duplication
   - Add documentation

**Total Estimated Time**: 3 hours
