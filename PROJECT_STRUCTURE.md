# Project Structure

> **Auto-generated on:** 2025-11-25  
> **Project:** Crypto Price Aggregator  
> **Last Updated:** Run `./update-structure.sh` to refresh this file

## Overview

This document provides a comprehensive view of the project's file structure. It's automatically generated and should be updated whenever significant structural changes are made.

---

## Root Directory

```
crypto-price-aggregator/
├── .agent/                    # Agent workflows and configurations
├── .github/                   # GitHub Actions CI/CD workflows
├── .gitignore                 # Git ignore patterns
├── CPAv2.md                   # Project plan and documentation
├── Dockerfile                 # Docker container configuration
├── SECURITY.md                # Security policies and guidelines
├── docker-compose.yml         # Docker Compose orchestration
├── pom.xml                    # Maven project configuration
├── PROJECT_STRUCTURE.md       # This file
├── update-structure.sh        # Script to regenerate this file
├── src/                       # Source code (see below)
└── target/                    # Build output (gitignored)
```

---

## Source Code Structure (`src/`)

### Main Application (`src/main/java/com/cryptoArb/`)

#### **Core Application**
```
├── CryptoPriceAggregatorApplication.java    # Spring Boot main application class
└── MemoryStress.java                        # Memory stress testing utility
```

#### **Adapter Layer** (Hexagonal Architecture)
```
adapter/
└── persistence/
    └── ExchangeInfoRepository.java          # JPA repository for exchange info
```

#### **Application Layer** (Hexagonal Architecture)
```
application/
├── controller/
│   ├── PriceController.java                 # REST API endpoints for prices
│   └── PriceGraphQLController.java          # GraphQL API endpoints
└── service/
    └── ExchangeInfoService.java             # Application service for exchange info
```

#### **Aspect-Oriented Programming**
```
aspect/
└── LoggingAspect.java                       # Cross-cutting logging concerns
```

#### **Configuration**
```
config/
├── AsyncConfig.java                         # Async task execution configuration
├── CacheConfig.java                         # L2 cache (Hibernate) configuration
├── CorsConfig.java                          # CORS policy configuration
├── CpaConfigProperties.java                 # Custom configuration properties
├── JpaConfig.java                           # JPA and auditing configuration
├── RedisConfig.java                         # Redis distributed cache config
├── Resilience4jConfig.java                  # Circuit breaker, retry, rate limiter
├── SecurityConfig.java                      # Security and authentication
└── WebConfig.java                           # Web MVC configuration
```

#### **Domain Layer** (Spring-based entities)
```
domain_spring/
├── ArbitrageOpportunity.java                # JPA entity for arbitrage opportunities
├── BaseEntity.java                          # Base entity with auditing fields
├── ConsolidatedPrice.java                   # JPA entity for consolidated prices
├── CurrencyPair.java                        # JPA entity for currency pairs
├── Exchange.java                            # Enum for supported exchanges
├── ExchangeInfo.java                        # JPA entity for exchange metadata
├── LinkedArbitrageOpportunity.java          # JPA entity with linked relationships
└── PriceTick.java                           # JPA entity for price ticks
```

#### **Exception Handling**
```
exception/
├── GlobalExceptionHandler.java              # Global REST exception handler
├── InvalidPairException.java                # Custom exception for invalid pairs
├── PriceFetchException.java                 # Custom exception for fetch failures
└── PriceNotFoundException.java              # Custom exception for missing prices
```

#### **Price Fetchers** (External API Integration)
```
fetcher/
├── BinanceFetcher.java                      # Binance API integration
├── CoinbaseFetcher.java                     # Coinbase API integration
└── PriceFetcher.java                        # Common interface for fetchers
```

#### **Java Implementation** (Legacy/Alternative implementations)
```
javaImpl/
├── domain_POJOs/                            # Plain Old Java Objects (legacy)
│   ├── ArbitrageOpportunity_POJO.java
│   ├── CurrencyPair_POJO.java
│   ├── Exchange_POJO.java
│   └── PriceTick_POJO.java
├── domain_records/                          # Java Records implementation
│   ├── ArbitrageOpportunity.java
│   ├── ConsolidatedPrice.java
│   ├── CurrencyPair.java
│   ├── Exchange.java
│   └── PriceTick.java
├── fetcher_javaImpl/                        # Alternative fetcher implementations
│   ├── BinanceFetcher.java
│   ├── CoinbaseFetcher.java
│   └── PriceFetcher.java
├── service_javaImpl/                        # Pure Java service implementations
│   ├── ArbitrageService.java
│   ├── DatabaseService.java
│   ├── OpportunityAggregator.java
│   ├── PriceEngineV1.java
│   ├── PriceEngineV2.java
│   ├── PriceFetcherFactory.java
│   ├── PriceService.java
│   └── SequentialPriceEngine.java
└── serviceOld/                              # Archived service versions
    ├── OpportunityAggregator_V1.java
    ├── OpportunityAggregator_V2.java
    ├── OpportunityAggregator_V3.java
    └── PriceEngineV2_old.java
```

#### **Observability**
```
observability/
├── actuator/
│   └── ExchangeHealthIndicator.java         # Custom health check for exchanges
└── metrics/
    └── CustomMetrics.java                   # Custom Micrometer metrics
```

#### **Repository Layer** (Data Access)
```
repository/
├── ArbitrageRepository.java                 # JPA repository for arbitrage data
├── LinkedArbitrageRepository.java           # Repository with custom queries
└── PriceTickRepository.java                 # JPA repository for price ticks
```

#### **Service Layer** (Business Logic)
```
service/
├── ArbitrageService.java                    # Interface for arbitrage service
├── PriceService.java                        # Interface for price service
├── SequentialPriceEngine.java               # Sequential price fetching engine
└── impl/
    ├── ArbitrageServiceImpl.java            # Arbitrage service implementation
    └── PriceServiceImpl.java                # Price service implementation
```

#### **Validation**
```
validation/
├── CurrencyPairValidator.java               # Custom validator for currency pairs
└── ValidCurrencyPair.java                   # Validation annotation
```

---

### Resources (`src/main/resources/`)

```
resources/
├── application.properties                   # Main application configuration
├── application-dev.properties               # Development profile configuration
├── logback-spring.xml                       # Logging configuration
└── graphql/
    └── schema.graphqls                      # GraphQL schema definition
```

---

### Test Code (`src/test/java/com/cryptoArb/`)

#### **Test Organization**

```
test/
├── CryptoPriceAggregatorApplicationTest.java    # Main application context test
├── MemoryStressTest.java                        # Memory stress tests
├── actuator/
│   └── ExchangeHealthIndicatorTest.java         # Health indicator tests
├── aspect/
│   └── LoggingAspectTest.java                   # AOP logging tests
├── config/
│   ├── CorsIntegrationTest.java                 # CORS configuration tests
│   ├── CpaConfigPropertiesTest.java             # Config properties tests
│   └── SecurityConfigTest.java                  # Security configuration tests
├── controller/
│   ├── PriceControllerTest.java                 # REST API tests
│   └── PriceGraphQLControllerTest.java          # GraphQL API tests
├── core/
│   ├── concurrency/                             # Concurrency pattern tests
│   │   ├── MyBlockingQueueTest.java
│   │   ├── TransactionIDLoggerTest.java
│   │   └── VolatileFlagStopTest.java
│   ├── datastructures/                          # Data structure tests
│   │   └── MyHashMapTest.java
│   └── internals/                               # Internal implementation tests
│       ├── PriceCacheTest.java
│       ├── SerializationTest.java
│       └── SimpleJsonSerializerTest.java
├── fetcher/
│   └── PriceFetcherContractTest.java            # Fetcher contract tests
├── integration/
│   ├── BaseIntegrationTest.java                 # Base class for integration tests
│   ├── L2CacheTest.java                         # Hibernate L2 cache tests
│   └── RedisIntegrationTest.java                # Redis integration tests
├── javaImpl/                                    # Tests for Java implementations
│   ├── domain_POJOs/
│   │   ├── CurrencyPairOldTest.java
│   │   ├── ExchangeOldTest.java
│   │   └── PriceTickOldTest.java
│   ├── domain_records/
│   │   ├── ArbitrageOpportunityTest.java
│   │   ├── ConsolidatedPriceTest.java
│   │   ├── CurrencyPairTest.java
│   │   ├── ExchangeTest.java
│   │   └── PriceTickTest.java
│   └── service_javaImpl/
│       ├── ArbitrageServiceTest.java
│       ├── DatabaseServiceTest.java
│       ├── OpportunityAggregatorTest.java
│       ├── PriceEngineV1Test.java
│       ├── PriceEngineV2Test.java
│       ├── PriceFetcherFactoryTest.java
│       ├── PriceServiceTest.java
│       └── SequentialPriceEngineTest.java
├── observability/
│   └── MetricsTest.java                         # Metrics tests
├── repository/
│   ├── ArbitrageRepositoryTest.java             # Repository tests
│   ├── NPlusOneProblemTest.java                 # N+1 query problem tests
│   └── PriceTickRepositoryTest.java             # Price tick repository tests
├── resilience/
│   └── ResilienceTest.java                      # Resilience4j tests
└── service/
    ├── SequentialPriceEngineTest.java           # Engine tests
    └── impl/
        ├── ArbitrageServiceImplTest.java        # Service implementation tests
        └── PriceServiceImplTest.java            # Service implementation tests
```

#### **Test Resources** (`src/test/resources/`)

```
test/resources/
└── logback-test.xml                             # Test logging configuration
```

---

## Architecture Overview

This project follows **Hexagonal Architecture** (Ports and Adapters) with clear separation of concerns:

### **Layers**

1. **Domain Layer** (`domain_spring/`)
   - Core business entities and value objects
   - JPA entities with auditing and soft delete support

2. **Application Layer** (`application/`)
   - Controllers (REST and GraphQL)
   - Application services
   - Orchestrates use cases

3. **Adapter Layer** (`adapter/`)
   - Persistence adapters (repositories)
   - External API adapters (fetchers)

4. **Infrastructure** (`config/`, `aspect/`, `observability/`)
   - Cross-cutting concerns
   - Configuration and setup
   - Monitoring and health checks

### **Key Design Patterns**

- **Repository Pattern**: Data access abstraction
- **Strategy Pattern**: Multiple price fetcher implementations
- **Factory Pattern**: `PriceFetcherFactory` for fetcher creation
- **Aspect-Oriented Programming**: Logging and cross-cutting concerns
- **Circuit Breaker Pattern**: Resilience4j for fault tolerance

### **Technology Stack**

- **Framework**: Spring Boot 3.x
- **Data Access**: Spring Data JPA, Hibernate
- **Caching**: Redis (L1), Hibernate L2 Cache
- **API**: REST (Spring Web), GraphQL (Spring GraphQL)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **Observability**: Spring Actuator, Micrometer
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Build**: Maven

---

## How to Update This File

Run the following command from the project root:

```bash
./update-structure.sh
```

This will regenerate the `PROJECT_STRUCTURE.md` file with the current project structure.

---

## Related Documentation

- [CPAv2.md](CPAv2.md) - Complete project plan and phase documentation
- [SECURITY.md](SECURITY.md) - Security policies and best practices
- [pom.xml](pom.xml) - Maven dependencies and build configuration

---

*This file is auto-generated. Manual edits may be overwritten.*
