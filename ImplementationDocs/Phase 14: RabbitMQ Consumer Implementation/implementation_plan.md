# Phase 14: RabbitMQ Consumer Implementation Plan

##  Overview

Building on the existing [PriceMessageProducer](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/service/PriceMessageProducer.java#20-65) that publishes `PriceTick` messages to RabbitMQ, we'll create a consumer service that listens for these messages, saves them to the database, and triggers arbitrage detection logic.

## User Review Required

> [!IMPORTANT]
> **Arbitrage Detection Logic**: This implementation will create arbitrage opportunities by querying recent price ticks from the database and comparing them. If you prefer a different arbitrage detection strategy (e.g., in-memory caching, event-driven architecture), please provide feedback.

> [!NOTE]
> **Testcontainers**: Integration tests will use Testcontainers for RabbitMQ and H2 for database. Tests will validate the entire message flow from producer → RabbitMQ → consumer → database.

## Proposed Changes

### Messaging Consumer

#### [NEW] [PriceTickConsumer.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/service/PriceTickConsumer.java)

**Purpose**: Listen for `PriceTick` messages from RabbitMQ and process them

**Key Features**:
- `@Service` and `@RabbitListener` annotations
- `processPriceTick(PriceTick tick)` method to handle incoming messages
- Save `PriceTick` to database via `PriceTickRepository`
- Call arbitrage detection logic  
- Comprehensive logging for debugging

**Design Decisions**:
- **Why @RabbitListener**: Spring's declarative messaging abstraction provides automatic message deserialization, error handling, and retry logic
- **Why save first, then detect**: Ensures data persistence before CPU-intensive arbitrage calculations
- **Error Handling**: Use `@RabbitListener` error handlers to prevent message loss

**Trade-offs**:
- **Pros**: Decouples price fetching from processing; scalable (multiple consumer instances); fault-tolerant
- **Cons**: Adds latency due to messaging layer; requires RabbitMQ infrastructure

---

### Arbitrage Detection Service

#### [MODIFY] [ArbitrageServiceImpl.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/service/impl/ArbitrageServiceImpl.java)

**Changes**:
- Add `detectAndSaveOpportunities(CurrencyPair pair)` method
- Query recent `PriceTicks` for the given pair
- Calculate arbitrage opportunities (max bid > min ask across exchanges)
- Save opportunities to `ArbitrageRepository`
- Make method `@Transactional` with `readOnly = false`

**Algorithm**:
```java
1. Query all PriceTicks for pair in last 60 seconds
2. Group by exchange  
3. Find exchange with highest bid (sell exchange)
4. Find exchange with lowest ask (buy exchange)
5. If bid > ask: create ArbitrageOpportunity and save
```

**Why this approach**:
- Simple and testable
- Database-backed ensures accuracy
- Time-window constraint prevents stale data

---

### Configuration Updates  

#### [MODIFY] [RabbitMqConfig.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/config/RabbitMqConfig.java)

**Optional Enhancement**:
- Add Dead Letter Queue (DLQ) configuration for failed messages
- Add retry policy with exponential backoff

---
  
## Verification Plan

### Automated Tests

#### Integration Test: RabbitMQ Message Flow

**Test File**: `src/test/java/com/cryptoArb/integration/RabbitMqMessagingIntegrationTest.java`

**Test Strategy**:
1. Use `@SpringBootTest` with Testcontainers for RabbitMQ and database
2. Inject `RabbitTemplate` to send test `PriceTick` messages
3. Use Awaitility to wait for async processing
4. Query `PriceTickRepository` to verify message was saved
5. Query `ArbitrageRepository` to verify arbitrage opportunity was detected

**Run Command**:
```bash
cd /home/pranay/anotherDrive/javaCodes/crypto-price-aggregator
mvn test -Dtest=RabbitMqMessagingIntegrationTest
```

**Expected Outcome**: All assertions pass, verifying end-to-end message flow

---

#### Unit Test: Arbitrage Detection Logic

**Test File**: [src/test/java/com/cryptoArb/service/impl/ArbitrageServiceImplTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/service/impl/ArbitrageServiceImplTest.java)

**Test Strategy**:
1. Mock `PriceTickRepository` to return test data
2. Call `detectAndSaveOpportunities(pair)`
3. Verify `ArbitrageRepository.save()` was called with correct data
4. Test edge cases: no arbitrage, single exchange, stale data

**Run Command**:
```bash
mvn test -Dtest=ArbitrageServiceImplTest
```

---

#### Existing Test: Producer Test

**Test File**: [src/test/java/com/cryptoArb/service/PriceMessageProducerTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/service/PriceMessageProducerTest.java)

**Run Command**:
```bash
mvn test -Dtest=PriceMessageProducerTest 
```

**Purpose**: Verify producer still works after consumer implementation

---

### Full Test Suite

**Run Command**:
```bash
mvn clean test
```

**Expected Outcome**: All tests pass, including new messaging tests and existing tests

---

### Manual Verification

Not applicable for this task - all verification can be automated.
