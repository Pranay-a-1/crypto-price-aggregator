# FlywayMigrationTest Fix Walkthrough

## Problem

All 4 tests in [FlywayMigrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#28-130) were failing with the error:
```
org.opentest4j.AssertionFailedError: expected: not <null>
```

The tests expected Flyway to be auto-configured and available as a Spring bean, but it was null.

## Root Cause Analysis

### Investigation Steps

1. **Checked test configuration**: The test was extending [BaseIntegrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/BaseIntegrationTest.java#30-86) and using the "test" profile
2. **Found Flyway disabled in test profile**: [application-test.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/resources/application-test.properties) had `spring.flyway.enabled=false`
3. **Initial fix attempt**: Added `@TestPropertySource` to override the property - **FAILED**
4. **Added `@DynamicPropertySource`**: Tried to override in the test class - **FAILED**
5. **Discovered the real issue**: [BaseIntegrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/BaseIntegrationTest.java#30-86) has its own `@DynamicPropertySource` method that explicitly sets `spring.flyway.enabled=false` at line 79

### Root Cause

[BaseIntegrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/BaseIntegrationTest.java#30-86) deliberately disables Flyway for all integration tests to allow test isolation using Hibernate's `create-drop` mode. Since [FlywayMigrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#28-130) extended [BaseIntegrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/BaseIntegrationTest.java#30-86), it inherited this configuration, and the `@DynamicPropertySource` from the parent class was preventing Flyway from being enabled.

## Solution

Refactored [FlywayMigrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#28-130) to **not extend [BaseIntegrationTest](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/BaseIntegrationTest.java#30-86)** and instead provide its own complete Testcontainers setup with Flyway enabled.

### Changes Made

#### [FlywayMigrationTest.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java)

**Key changes:**

1. **Removed inheritance**: Changed from `extends BaseIntegrationTest` to standalone test class
2. **Added Testcontainers annotations**: `@Testcontainers` and `@Container` for PostgreSQL
3. **Created dedicated PostgreSQL container**:
   ```java
   @Container
   static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
           .withDatabaseName("cryptodb_test")
           .withUsername("test")
           .withPassword("test");
   ```

4. **Configured properties to enable Flyway**:
   ```java
   @DynamicPropertySource
   static void configureProperties(DynamicPropertyRegistry registry) {
       // PostgreSQL datasource from Testcontainer
       registry.add("spring.datasource.url", postgres::getJdbcUrl);
       
       // CRITICAL: Enable Flyway
       registry.add("spring.flyway.enabled", () -> "true");
       
       // Set Hibernate to validate mode (let Flyway manage schema)
       registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
       
       // Flyway configuration
       registry.add("spring.flyway.baseline-on-migrate", () -> "true");
       registry.add("spring.flyway.baseline-version", () -> "0");
   }
   ```

## Verification

### FlywayMigrationTest Results

All 4 tests passed:
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

✅ [shouldHaveFlywayConfigured](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#72-77) - Verifies Flyway bean is available  
✅ [shouldRunMigrationsSuccessfully](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#78-104) - Verifies V1 and V2 migrations executed  
✅ [shouldHaveCorrectMigrationState](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#105-119) - Verifies no pending migrations  
✅ [shouldValidateMigrationsWithoutErrors](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/test/java/com/cryptoArb/crypto_price_aggregator/FlywayMigrationTest.java#120-129) - Validates migration integrity  

### Full Test Suite

Ran the entire test suite to ensure no regressions:
```bash
mvn clean test -DSPRING_PROFILES_ACTIVE=test
```

Results:
```
[INFO] Tests run: 160, Failures: 0, Errors: 0, Skipped: 1
[INFO] BUILD SUCCESS
```

All 160 tests pass successfully!

## Summary

The fix resolved the FlywayMigrationTest failures by creating a standalone test configuration that enables Flyway, separating it from the standard integration test setup that deliberately disables Flyway for test isolation. This approach allows:

- **Flyway migration tests** to verify database migrations work correctly
- **Other integration tests** to continue using Hibernate's create-drop for fast, isolated testing
- **No regression** in the existing test suite

