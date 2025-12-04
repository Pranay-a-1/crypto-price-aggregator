# Phase 9: Database Migration to PostgreSQL - Task Breakdown

## TDD Cycles

### Cycle 1: PostgreSQL Docker Setup
- [x] RED: Create integration test expecting PostgreSQL connection (fail without PostgreSQL)
- [x] GREEN: Add PostgreSQL to docker-compose.yml
- [x] GREEN: Create application-prod.properties with PostgreSQL config
- [x] REFACTOR: Verify docker-compose services start correctly

### Cycle 2: PostgreSQL Dependency and Driver
- [x] RED: Run integration test with PostgreSQL (fail on missing driver)
- [x] GREEN: Add PostgreSQL dependency to pom.xml
- [x] GREEN: Update JPA dialect to PostgreSQL in application-prod.properties
- [x] REFACTOR: Ensure H2 still works for dev profile

### Cycle 3: Testcontainers Integration
- [x] RED: Write integration test using Testcontainers (fail without dependency)
- [x] GREEN: Add Testcontainers dependency to pom.xml
- [x] GREEN: Create base test class with PostgreSQL Testcontainer
- [x] GREEN: Update PriceTickRepositoryTest to use Testcontainers
- [x] REFACTOR: Extract common Testcontainers configuration

### Cycle 4: Transaction Management
- [x] RED: Write test for transactional behavior (fail without @Transactional)
- [x] GREEN: Add @Transactional to PriceServiceImpl
- [x] GREEN: Add @Transactional to PriceTickConsumer
- [x] REFACTOR: Review transaction boundaries

### Cycle 5: PostgreSQL-Specific Queries
- [x] RED: Test PostgreSQL-specific features if needed (fail on H2 dialect)
- [x] GREEN: Update repository queries for PostgreSQL compatibility
- [x] REFACTOR: Ensure queries work on both H2 (dev) and PostgreSQL (prod)

### Cycle 6: End-to-End Verification
- [x] Run all existing tests with H2 (dev profile)
- [x] Run all tests with Testcontainers (PostgreSQL)
- [x] Start application with PostgreSQL via docker-compose
- [x] Manually verify data persistence
- [x] Document verification in walkthrough
