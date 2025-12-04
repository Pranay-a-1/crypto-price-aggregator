# Phase 9: Database Migration to PostgreSQL

Migrate the CryptoPriceAggregator from H2 in-memory database to production-ready PostgreSQL with Docker support and Testcontainers for integration testing.

## User Review Required

> [!IMPORTANT]
> This migration will introduce PostgreSQL as a production database while maintaining H2 for development and quick testing. The application will use profile-based configuration to switch between databases.

> [!WARNING]
> Docker must be running for production mode and for running integration tests with Testcontainers. Ensure Docker is installed and accessible.

## Proposed Changes

### Infrastructure: Database Container

#### [NEW] [docker-compose.yml](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/docker-compose.yml)

**Changes:**
- Add PostgreSQL service with persistent volume
- Configure PostgreSQL with database credentials
- Add health check for PostgreSQL
- Update app service to depend on PostgreSQL

**Why:**
- Provide production-grade persistent database
- Enable local development with production-like environment
- Ensure database is ready before application starts

---

### Configuration: Production Properties

#### [NEW] [application-prod.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application-prod.properties)

**Changes:**
- Configure PostgreSQL datasource URL, username, password
- Set PostgreSQL dialect
- Configure connection pool settings
- Set DDL mode to `validate` (production-safe)
- Disable H2 console

**Why:**
- Isolate production configuration from development
- Use production-ready database settings
- Prevent accidental schema drops in production

---

### Dependencies: PostgreSQL and Testcontainers

#### [MODIFY] [pom.xml](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/pom.xml)

**Changes:**
- Add PostgreSQL JDBC driver dependency (runtime scope)
- Add Testcontainers BOM for dependency management
- Add Testcontainers PostgreSQL module (test scope)
- Add Testcontainers JUnit Jupiter integration (test scope)

**Why:**
- Enable PostgreSQL connectivity
- Support integration testing with real PostgreSQL instances
- Ensure consistent Testcontainers versions

---

### Service Layer: Transaction Management

#### [MODIFY] [PriceServiceImpl.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/PriceServiceImpl.java)

**Changes:**
- Add `@Transactional` annotation at class level

**Why:**
- Ensure atomic database operations
- Enable proper rollback on exceptions
- Required for production-grade data persistence

#### [MODIFY] [PriceTickConsumer.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceTickConsumer.java)

**Changes:**
- Add `@Transactional` annotation to message handler methods

**Why:**
- Ensure message processing and database updates are atomic
- Prevent partial data commits on failures

---

### Testing: Testcontainers Integration

#### [NEW] [BaseIntegrationTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/BaseIntegrationTest.java)

**Changes:**
- Create abstract base class for integration tests
- Configure PostgreSQL Testcontainer with reuse enabled
- Set up dynamic Spring properties from Testcontainer
- Provide shared configuration for all integration tests

**Why:**
- Centralize Testcontainers configuration
- Enable reuse of containers across test classes for speed
- Ensure tests run against real PostgreSQL

#### [MODIFY] [PriceTickRepositoryTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepositoryTest.java)

**Changes:**
- Extend `BaseIntegrationTest` instead of using H2
- Remove H2-specific test annotations if any
- Verify all queries work with PostgreSQL

**Why:**
- Test repository layer with production database
- Catch PostgreSQL-specific issues early
- Validate query compatibility

#### [NEW] [PostgreSQLIntegrationTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/PostgreSQLIntegrationTest.java)

**Changes:**
- Create comprehensive integration test for PostgreSQL
- Test connection, schema creation, CRUD operations
- Verify transaction handling
- Test concurrent access scenarios

**Why:**
- Validate complete PostgreSQL integration
- Serve as proof that migration works
- Document expected behavior

---

### Repository Layer: PostgreSQL Compatibility

#### [MODIFY] [PriceTickRepository.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/repository/PriceTickRepository.java)

**Changes:**
- Review and update queries for PostgreSQL compatibility
- Ensure native queries use standard SQL or PostgreSQL dialect
- Add indexes if needed for production performance

**Why:**
- Ensure queries work efficiently on PostgreSQL
- Avoid H2-specific SQL syntax
- Optimize for production workloads

## Verification Plan

### Automated Tests

1. **Unit Tests:** Run existing unit tests with H2 (no changes expected)
   ```bash
   mvn test -Dspring.profiles.active=dev
   ```

2. **Integration Tests:** Run integration tests with Testcontainers
   ```bash
   mvn test -Dtest=*IntegrationTest
   ```

3. **Full Test Suite:** Run all tests
   ```bash
   mvn clean test
   ```

### Manual Verification

1. **Docker PostgreSQL Setup:**
   ```bash
   docker-compose up -d db
   docker-compose logs db  # Verify PostgreSQL started
   ```

2. **Run Application with PostgreSQL:**
   ```bash
   docker-compose up app
   # OR locally:
   SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
   ```

3. **Test REST Endpoints:**
   - Fetch price data: `curl http://localhost:8080/api/prices/BTC-USD`
   - Check health: `curl http://localhost:8080/actuator/health`
   - Verify database health indicator shows PostgreSQL

4. **Verify Data Persistence:**
   - Connect to PostgreSQL: `docker exec -it <container> psql -U cryptouser -d cryptodb`
   - Query tables: `SELECT * FROM price_tick LIMIT 10;`
   - Verify data persists after app restart

5. **Profile Switching:**
   - Start with dev profile (H2): `./mvnw spring-boot:run`
   - Start with prod profile (PostgreSQL): `SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run`
   - Confirm correct database is used in logs
