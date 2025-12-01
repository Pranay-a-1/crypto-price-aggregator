# Phase 3: In-Memory Persistence with H2 and Basic CRUD

## Overview

This phase adds persistence to the CryptoPriceAggregator by integrating H2 in-memory database and Spring Data JPA. Currently, Phase 2 fetches prices concurrently from multiple mock exchanges, but all data is lost on restart. Phase 3 introduces a repository layer to store [PriceTick](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTick.java#16-60) data and query it for aggregation.

### Why This Phase Matters

**Problem:** In-memory fetching (Phase 2) loses data on restart and cannot support historical queries or trend analysis.

**Solution:** Persistence enables:
- Historical price queries  
- Trend analysis over time windows
- Data survival across application restarts
- Foundation for future features (arbitrage detection, analytics)

**Limitation:** Service still calls repository directly (tight coupling), which we'll address with events in Phase 5.

---

## User Review Required

> [!IMPORTANT]
> **JPA Entity Design Decision**
> 
> [PriceTick](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTick.java#16-60) is currently a Java **record** (immutable by default). JPA entities typically require:
> - A no-arg constructor (records don't have one)
> - Mutable fields (for Hibernate proxies)
> 
> **Two Options:**
> 1. **Convert to regular class** with Lombok `@Value` or `@Data` (recommended for JPA)
> 2. **Keep as record** with JPA workarounds (requires Hibernate 6+ and special configuration)
> 
> I recommend **Option 1** for clarity and compatibility. We'll use Lombok to maintain immutability semantics while satisfying JPA requirements.

> [!WARNING]
> **H2 is for Development Only**
> 
> H2 is an in-memory database that:
> - Loses all data when the application stops
> - Is not suitable for production
> - Phase 10 will migrate to PostgreSQL for production persistence
> 
> This is intentional for learning: we start simple and evolve.

---

## Proposed Changes

### Dependencies

#### [MODIFY] [pom.xml](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/pom.xml)

Add Spring Data JPA and H2 dependencies:
```xml
<!-- Spring Data JPA for repository abstraction -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 In-Memory Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### Domain Layer - JPA Entities

#### [MODIFY] [CurrencyPair.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/CurrencyPair.java)

Convert from record to embeddable JPA component:
- Change from `record` to regular class with `@Embeddable`
- Add no-arg constructor for JPA
- Use Lombok `@Value` for immutability
- Rename fields to `base` and `quote` (remove `pair` prefix to avoid nested naming)

#### [MODIFY] [PriceTick.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/domain/PriceTick.java)

Convert from record to JPA entity:
- Change from `record` to regular class
- Add `@Entity` and `@Table(name = "price_ticks")`
- Add `@Id @GeneratedValue` for `id` field (Long)
- Embed `CurrencyPair` with `@Embedded`
- Store `Exchange` as enum (use `@Enumerated(EnumType.STRING)`)
- Use Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Keep validation in custom constructor
- Add `@Column` annotations for BigDecimal precision

---

### Repository Layer

#### [NEW] [PriceTickRepository.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java)

Create Spring Data JPA repository interface:
```java
public interface PriceTickRepository extends JpaRepository<PriceTick, Long> {
    
    // Find ticks for a specific currency pair
    List<PriceTick> findByPair_BaseAndPair_Quote(String base, String quote);
    
    // Find recent ticks for a pair (used for aggregation)
    List<PriceTick> findByPair_BaseAndPair_QuoteAndTimestampAfter(
        String base, String quote, Instant timestamp
    );
    
    // Find ticks by exchange
    List<PriceTick> findByExchange(Exchange exchange);
    
    // Custom query for recent ticks across all exchanges
    @Query("SELECT pt FROM PriceTick pt WHERE pt.timestamp > :since ORDER BY pt.timestamp DESC")
    List<PriceTick> findRecentTicks(@Param("since") Instant since);
}
```

---

### Configuration

#### [NEW] [JpaConfig.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/config/JpaConfig.java)

Configure JPA repositories and transaction management:
- Enable JPA repositories with `@EnableJpaRepositories`
- Enable transaction management with `@EnableTransactionManagement`
- Set entity scan package

#### [MODIFY] [application.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application.properties)

Add H2 configuration:
```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

#### [NEW] [application-dev.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application-dev.properties)

Create dev-specific properties:
```properties
# Development Profile Configuration

# Enable H2 Console for debugging
spring.h2.console.enabled=true

# More verbose logging for development
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

### Service Layer Updates

#### [MODIFY] [PriceServiceImpl.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/PriceServiceImpl.java)

Update service to use repository for persistence:
- Inject `PriceTickRepository` via constructor
- After fetching ticks, save them to database using `repository.saveAll()`
- Query recent ticks from database for aggregation (last 5 seconds)
- Update aggregation logic to use persisted data
- Add `@Transactional` annotation for transaction management

**Key Changes:**
1. **Save all fetched ticks** before aggregation
2. **Query from database** instead of using in-memory list
3. **Time-window filtering**: Only consider ticks from the last 5 seconds

---

### Testing

#### [NEW] [PriceTickRepositoryTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepositoryTest.java)

Unit tests for repository with `@DataJpaTest`:
- Test save and findAll
- Test findByPair methods
- Test findByExchange
- Test findRecentTicks with time filtering
- Test empty results

#### [MODIFY] [PriceServiceImplTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/service/PriceServiceTest.java)

Update unit tests to mock repository:
- Mock `PriceTickRepository`
- Verify `saveAll()` is called after fetching
- Verify queries use correct time windows
- Test aggregation with persisted data

#### [NEW] [PriceServiceIntegrationTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/service/PriceServiceIntegrationTest.java)

End-to-end integration test with `@SpringBootTest`:
- Test full flow: REST endpoint → Service → Repository → H2
- Verify data persistence across service calls
- Test concurrent fetching with database saves
- Verify aggregation uses database queries

---

## Verification Plan

### Automated Tests

```bash
# Run all tests
./mvnw clean test

# Run specific test classes
./mvnw test -Dtest=PriceTickRepositoryTest
./mvnw test -Dtest=PriceServiceImplTest
./mvnw test -Dtest=PriceServiceIntegrationTest

# Verify coverage
./mvnw test jacoco:report
```

### Manual Verification

1. **Start the application with dev profile:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Access H2 Console:**
   - Open browser: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`, Password: (empty)

3. **Test REST endpoint:**
   ```bash
   # Fetch prices for BTC/USD
   curl http://localhost:8080/api/prices/BTC-USD
   
   # Call multiple times and verify data accumulates
   curl http://localhost:8080/api/prices/BTC-USD
   curl http://localhost:8080/api/prices/ETH-USD
   ```

4. **Verify in H2 Console:**
   ```sql
   -- Check all price ticks
   SELECT * FROM price_ticks ORDER BY timestamp DESC;
   
   -- Count ticks per pair
   SELECT base, quote, COUNT(*) FROM price_ticks GROUP BY base, quote;
   
   -- Verify recent ticks (last 5 seconds)
   SELECT * FROM price_ticks 
   WHERE timestamp > DATEADD('SECOND', -5, CURRENT_TIMESTAMP());
   ```

### Success Criteria

- ✅ All unit tests pass
- ✅ All integration tests pass
- ✅ PriceTicks are saved to H2 database after fetching
- ✅ Aggregation queries the database correctly
- ✅ H2 console shows persisted data
- ✅ Multiple REST calls accumulate data in database
- ✅ Time-window filtering works correctly (last 5 seconds)
