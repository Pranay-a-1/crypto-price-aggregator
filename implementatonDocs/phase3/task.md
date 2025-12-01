# Phase 3: In-Memory Persistence with H2 and Basic CRUD

## Goal
Store fetched ticks in H2 database and query them for aggregation, adding a repository layer.

## Task Breakdown

### 1. Project Setup
- [x] Add Spring Data JPA dependency to [pom.xml](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/pom.xml)
- [x] Add H2 database dependency to [pom.xml](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/pom.xml)
- [x] Configure H2 in [application.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application.properties) (enable console, set dialect)
- [x] Create [application-dev.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application-dev.properties) for dev-specific config

### 2. Domain Layer - JPA Entities (TDD Cycle 1)
- [x] RED: Write test for [PriceTick](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTick.java#21-134) entity persistence (expect failure - no @Entity)
- [x] GREEN: Convert [PriceTick](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTick.java#21-134) from record to JPA entity class
  - [x] Add `@Entity`, `@Table` annotations
  - [x] Add `@Id` and `@GeneratedValue` for primary key
  - [x] Convert [CurrencyPair](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/CurrencyPair.java#21-51) to `@Embeddable`
  - [x] Handle [Exchange](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java#49-56) enum persistence
  - [x] Keep immutability with final fields and Lombok
- [x] REFACTOR: Ensure validation remains, optimize JPA annotations
- [x] Verify all tests pass (62 tests passing)

### 3. Repository Layer (TDD Cycle 2)
- [x] RED: Write [PriceTickRepositoryTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepositoryTest.java#24-181) - test save and findAll (expect failure)
- [x] GREEN: Create [PriceTickRepository](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java#24-66) interface extending `JpaRepository`
- [x] REFACTOR: Add custom query methods
  - [x] `findByPairBaseAndPairQuote`
  - [x] `findByPairBaseAndPairQuoteAndTimestampAfter`
  - [x] [findByExchange](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java#49-56)
  - [x] [findRecentTicks](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java#57-65) (custom @Query)
- [x] Verify all repository tests pass (7 tests)

### 4. JPA Configuration (TDD Cycle 3)
- [x] GREEN: Create [JpaConfig](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/config/JpaConfig.java#18-25) class
  - [x] Enable JPA repositories
  - [x] Configure transaction management
  - [x] Set up entity scanning

### 5. Service Layer Update (TDD Cycle 4)
- [x] Update [PriceServiceImpl](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/PriceServiceImpl.java#48-145) to use [PriceTickRepository](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java#24-66)
  - [x] Inject repository via constructor
  - [x] Save fetched ticks to database
  - [x] Query from database for aggregation
  - [x] Handle time-window queries (e.g., last 5 seconds)
- [x] Update [PriceServiceTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/service/PriceServiceTest.java#27-354) to mock repository
- [x] REFACTOR: Extract constants for time windows
- [x] Verify all tests pass (67 total tests)

### 6. Integration Tests (TDD Cycle 5)
- [ ] Create `PriceServiceIntegrationTest`
  - [ ] Use `@SpringBootTest` for full integration
  - [ ] Test save → query → aggregate flow
  - [ ] Verify H2 persistence and retrieval

### 7. H2 Console and Manual Testing
- [ ] Enable H2 console in dev profile
- [ ] Start application and verify H2 console access
- [ ] Manually test REST endpoint with persistence
- [ ] Verify data in H2 console

### 8. Documentation and Cleanup
- [ ] Update [PROJECT_STRUCTURE.md](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/PROJECT_STRUCTURE.md)
- [ ] Create Phase 3 summary document
- [ ] Document JPA entity design decisions
- [ ] Add code comments for complex queries

## Key Principles
- **TDD First**: Write failing test before implementation
- **Red-Green-Refactor**: Follow the cycle strictly
- **SOLID**: Keep repository, service, and controller concerns separated
- **Immutability**: Maintain defensive programming even with JPA
- **No Over-Engineering**: Keep it simple (KISS), avoid unnecessary abstractions (YAGNI)

## Testing Strategy
1. Unit tests for repository methods
2. Integration tests for JPA configuration
3. End-to-end tests for REST → Service → Repository → Database
4. Manual verification via H2 console
