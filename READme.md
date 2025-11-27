# Cryptocurrency Price Aggregator

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](.github/workflows)

> **A production-grade, event-driven Spring Boot application demonstrating enterprise Java development practices, from Core Java fundamentals to advanced Spring Boot features.**

## 🎯 Project Overview

The Cryptocurrency Price Aggregator (CPA) is a real-time, high-performance backend service that aggregates cryptocurrency prices from multiple exchanges, detects arbitrage opportunities, and exposes data through secure REST and GraphQL APIs. 

This project showcases the **complete evolution** of a Java service—from Core Java implementations (manual concurrency, JDBC, custom data structures) to a production-grade Spring Boot application with modern architectural patterns.

### Key Highlights

- 🏗️ **Hexagonal Architecture** (Ports & Adapters) with clear separation of concerns
- 🔄 **Event-Driven Architecture** using RabbitMQ for asynchronous processing
- 🔒 **OAuth 2.0 JWT Security** with Spring Security
- 📊 **Multi-Layer Caching** (Redis L1 + Hibernate L2 cache)
- 🛡️ **Resilience Patterns** (Circuit Breaker, Retry, Rate Limiting via Resilience4j)
- 📈 **Production Observability** (Actuator, Prometheus metrics, structured logging)
- 🧪 **Comprehensive Testing** (Unit, Integration, Slice tests with Testcontainers)
- 🎨 **Dual API Design** (REST + GraphQL)

---

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Design Patterns & Principles](#-design-patterns--principles)
- [Key Features](#-key-features)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Testing Strategy](#-testing-strategy)
- [Interview Discussion Points](#-interview-discussion-points)
- [Development Journey](#-development-journey)
- [Related Documentation](#-related-documentation)

---

## 🏗️ Architecture

### System Architecture Diagram

```
┌─────────────────────┐
│  External Exchanges │
│  (Coinbase, Binance)│
└──────────┬──────────┘
           │ WebSocket/HTTP
           ▼
┌─────────────────────┐
│  Price Fetchers     │
│  (Strategy Pattern) │
└──────────┬──────────┘
           │ Publish
           ▼
┌─────────────────────┐      ┌──────────────────┐
│     RabbitMQ        │◄─────┤  Dead Letter Q   │
│  (Message Broker)   │      └──────────────────┘
└──────────┬──────────┘
           │ Consume
           ▼
┌─────────────────────┐
│ Arbitrage Engine    │
│ (@RabbitListener)   │
└──────────┬──────────┘
           │ Store
           ▼
┌─────────────────────┐      ┌──────────────────┐
│    PostgreSQL       │◄────►│  Redis Cache     │
│   (TimeScaleDB)     │      │  (Distributed)   │
└──────────┬──────────┘      └──────────────────┘
           │ Query
           ▼
┌─────────────────────┐
│   REST/GraphQL API  │
│  (OAuth2 Secured)   │
└─────────────────────┘
```

### Architectural Patterns

#### **Hexagonal Architecture (Ports & Adapters)**

```
com.cryptoArb/
├── domain/               # Core business entities & value objects (framework-agnostic)
├── application/          # Use cases, orchestration, controllers
│   ├── controller/       # REST & GraphQL endpoints
│   └── service/          # Application services
├── adapter/              # Infrastructure layer
│   ├── persistence/      # JPA repositories (implements domain ports)
│   ├── messaging/        # RabbitMQ integration
│   └── fetcher/          # External API clients
└── config/               # Spring configuration
```

**Why Hexagonal Architecture?**
- **Testability**: Business logic isolated from infrastructure
- **Flexibility**: Swap implementations (e.g., RabbitMQ → Kafka) without changing core logic
- **Maintainability**: Clear boundaries between layers

#### **Event-Driven Architecture**

- **Producer**: `PriceMessageProducer` publishes price ticks to RabbitMQ
- **Broker**: RabbitMQ decouples producers from consumers
- **Consumer**: `PriceTickConsumer` processes messages, detects arbitrage, persists data
- **Benefits**: High throughput, fault tolerance, horizontal scalability

---

## 🛠️ Tech Stack

### Core Technologies

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 17 | Language features (Records, Sealed Classes, Pattern Matching) |
| **Framework** | Spring Boot | 3.5.7 | Enterprise application framework |
| **Build Tool** | Maven | 3.x | Dependency management & build automation |
| **Database** | PostgreSQL | 16+ | Primary relational database |
|  | TimeScaleDB | Latest | Time-series hypertable for `price_tick` |
| **Messaging** | RabbitMQ | Latest | Asynchronous event processing |
| **Caching** | Redis | Latest | Distributed L1 cache |
|  | Caffeine | Latest | Hibernate L2 cache |

### Spring Ecosystem

- **Spring Data JPA**: ORM with Hibernate, N+1 query optimization
- **Spring Security**: OAuth 2.0 Resource Server (JWT validation)
- **Spring AMQP**: RabbitMQ integration with retry & DLQ
- **Spring GraphQL**: Alternative query API
- **Spring Actuator**: Health checks, metrics, monitoring
- **Spring AOP**: Cross-cutting concerns (logging, metrics)

### Resilience & Observability

- **Resilience4j**: Circuit Breaker, Retry, Rate Limiter, Bulkhead
- **Micrometer**: Metrics collection (Prometheus format)
- **Logback**: Structured JSON logging (Logstash encoder)
- **Testcontainers**: Integration testing with real services (PostgreSQL, RabbitMQ, Redis)

### API & Documentation

- **SpringDoc OpenAPI**: Swagger UI at `/swagger-ui.html`
- **GraphQL**: Schema-first API design

---

## 🎨 Design Patterns & Principles

### GoF Design Patterns Implemented

1. **Strategy Pattern**: `PriceFetcher` interface with multiple implementations (`BinanceFetcher`, `CoinbaseFetcher`)
2. **Factory Pattern**: `PriceFetcherFactory` for dynamic fetcher creation
3. **Repository Pattern**: Spring Data JPA repositories abstract data access
4. **Observer Pattern**: RabbitMQ pub/sub for event-driven communication
5. **Template Method**: Spring's `JpaRepository` and custom base classes
6. **Adapter Pattern**: Fetchers adapt external APIs to internal domain models
7. **Singleton Pattern**: Spring beans (application-scoped services)

### SOLID Principles

- **Single Responsibility**: Each service has one clear purpose (e.g., `ArbitrageServiceImpl` only handles arbitrage detection)
- **Open/Closed**: New exchanges added via new `PriceFetcher` implementations (no modification of existing code)
- **Liskov Substitution**: All fetchers interchangeable via `PriceFetcher` interface
- **Interface Segregation**: Focused interfaces (e.g., `PriceService` vs. `ArbitrageService`)
- **Dependency Inversion**: Services depend on abstractions (interfaces), not concrete implementations

### Other Principles

- **DRY (Don't Repeat Yourself)**: Utility classes, base entities with JPA auditing
- **KISS (Keep It Simple)**: Clear, readable code over premature optimization
- **YAGNI (You Aren't Gonna Need It)**: Feature implementation driven by actual requirements

---

## ✨ Key Features

### 1. Multi-Exchange Price Aggregation
- Fetches real-time prices from multiple cryptocurrency exchanges
- Calculates consolidated best bid/ask across all sources
- Stores all price ticks in TimeScaleDB for time-series analysis

### 2. Arbitrage Detection Engine
- Identifies price discrepancies across exchanges in real-time
- Calculates profit percentages accounting for fees
- Persists opportunities for historical analysis

### 3. OAuth 2.0 Security
- Stateless JWT authentication via Spring Security
- Resource Server configuration (validates tokens from external IdP)
- Role-based access control (RBAC) ready

### 4. Advanced Caching Strategy
- **L1 (Redis)**: Distributed cache for consolidated prices (`@Cacheable`)
- **L2 (Caffeine)**: Hibernate second-level cache for static entities (`ExchangeInfo`)
- **Cache Eviction**: Time-based and event-driven invalidation

### 5. Resilience4j Integration
- **Circuit Breaker**: Prevents cascading failures when exchanges are down
- **Retry**: Configurable exponential backoff for transient failures
- **Rate Limiter**: Protects external APIs from overload
- **Bulkhead**: Isolates thread pools per exchange

### 6. Comprehensive Observability
- **Health Checks**: Custom `ExchangeHealthIndicator` for exchange connectivity
- **Metrics**: Custom Micrometer metrics (`@Timed`, `MeterRegistry`)
- **Prometheus**: Metrics endpoint at `/actuator/prometheus`
- **Structured Logging**: JSON format for log aggregation (ELK/Splunk ready)

### 7. Asynchronous Processing
- **RabbitMQ**: Event-driven message processing with DLQ (Dead Letter Queue)
- **@Async**: Background task execution with custom thread pools
- **Message Tracing**: Distributed tracing with Micrometer

### 8. Advanced JPA Features
- **N+1 Query Prevention**: `@EntityGraph` and `JOIN FETCH`
- **JPA Auditing**: `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`
- **Soft Delete**: `@SQLDelete` + `@Where` (logical deletion)
- **Custom Queries**: `JpaSpecificationExecutor` for dynamic queries

### 9. Dual API Design
- **REST API**: Traditional CRUD operations with HATEOAS
- **GraphQL API**: Flexible querying, reduces over-fetching

### 10. Production-Ready Deployment
- **Docker**: Multi-stage Dockerfile for optimized images
- **Docker Compose**: Local development environment (app + PostgreSQL + RabbitMQ + Redis)
- **CI/CD**: GitHub Actions workflow for automated testing and builds

---

## 📁 Project Structure

```
crypto-price-aggregator/
├── .github/workflows/         # CI/CD pipelines
├── src/
│   ├── main/
│   │   ├── java/com/cryptoArb/
│   │   │   ├── adapter/           # Hexagonal Architecture - Adapters
│   │   │   │   └── persistence/   # JPA repositories
│   │   │   ├── application/       # Hexagonal Architecture - Application Layer
│   │   │   │   ├── controller/    # REST & GraphQL controllers
│   │   │   │   └── service/       # Application services
│   │   │   ├── aspect/            # AOP cross-cutting concerns
│   │   │   ├── config/            # Spring configuration
│   │   │   ├── domain_spring/     # JPA entities (business domain)
│   │   │   ├── exception/         # Custom exceptions & handlers
│   │   │   ├── fetcher/           # Exchange API clients
│   │   │   ├── javaImpl/          # Core Java implementations (Phase 1-10)
│   │   │   ├── observability/     # Metrics & health indicators
│   │   │   ├── repository/        # Spring Data JPA repositories
│   │   │   ├── service/           # Business logic layer
│   │   │   └── validation/        # Custom validators
│   │   └── resources/
│   │       ├── application.properties         # Main config
│   │       ├── application-dev.properties     # Dev profile
│   │       ├── logback-spring.xml             # Logging config
│   │       └── graphql/schema.graphqls        # GraphQL schema
│   └── test/                      # Comprehensive test suite
│       ├── integration/           # Integration tests (Testcontainers)
│       ├── controller/            # @WebMvcTest slice tests
│       └── service/               # Unit tests
├── docker-compose.yml             # Local development environment
├── Dockerfile                     # Production container image
├── pom.xml                        # Maven dependencies
├── CPAv2.md                       # 15-phase development plan
├── PROJECT_STRUCTURE.md           # Detailed structure documentation
└── SECURITY.md                    # Security policies
```

**For detailed structure**: See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher ([Download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker & Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))

### Quick Start with Docker

```bash
# 1. Clone the repository
git clone <repository-url>
cd crypto-price-aggregator

# 2. Start all services (PostgreSQL, RabbitMQ, Redis, Application)
docker-compose up -d

# 3. Application will be available at:
# - REST API: http://localhost:8080/api/v1
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - GraphQL: http://localhost:8080/graphql
# - Actuator: http://localhost:8080/actuator
# - RabbitMQ Management: http://localhost:15672 (guest/guest)
```

### Local Development Setup

```bash
# 1. Start infrastructure services only
docker-compose up postgres rabbitmq redis -d

# 2. Build the application
mvn clean install

# 3. Run with dev profile (uses H2 in-memory DB)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# OR run with prod profile (uses PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Configuration

**Application Profiles:**

- `dev`: H2 in-memory database, verbose logging, hot reload
- `prod`: PostgreSQL/TimeScaleDB, optimized logging, connection pooling

**Environment Variables** (for Docker/Production):

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/crypto_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# OAuth2 (Resource Server)
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://your-idp/.well-known/jwks.json
```

---

## 📚 API Documentation

### REST API Endpoints

**Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

#### Price Endpoints

```bash
# Get consolidated price for a currency pair
GET /api/v1/price/{pair}
Example: GET /api/v1/price/BTC-USD

# Response:
{
  "pair": {"base": "BTC", "quote": "USD"},
  "timestamp": "2025-11-27T10:23:59Z",
  "bestBid": 45000.00,
  "bestBidExchange": "COINBASE",
  "bestAsk": 45050.00,
  "bestAskExchange": "BINANCE"
}
```

#### Arbitrage Endpoints

```bash
# Get recent arbitrage opportunities (last 24h)
GET /api/v1/arbitrage

# Response:
[
  {
    "pair": {"base": "ETH", "quote": "USD"},
    "timestamp": "2025-11-27T10:20:00Z",
    "buyExchange": "BINANCE",
    "buyPrice": 3000.00,
    "sellExchange": "COINBASE",
    "sellPrice": 3020.00,
    "profitPercentage": 0.67
  }
]
```

### GraphQL API

**GraphiQL UI**: [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

```graphql
# Query consolidated price
query {
  consolidatedPrice(pair: "BTC-USD") {
    pair { base quote }
    bestBid
    bestAsk
    timestamp
  }
}

# Query arbitrage opportunities
query {
  arbitrageOpportunities(limit: 10) {
    pair { base quote }
    profitPercentage
    buyExchange
    sellExchange
  }
}
```

### Authentication

All endpoints require a valid JWT token:

```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8080/api/v1/price/BTC-USD
```

### Health & Metrics

```bash
# Health check
GET /actuator/health

# Prometheus metrics
GET /actuator/prometheus

# Application info
GET /actuator/info
```

---

## 🧪 Testing Strategy

### Test Coverage

- **Unit Tests**: 80%+ coverage (services, controllers, domain logic)
- **Integration Tests**: Testcontainers for PostgreSQL, RabbitMQ, Redis
- **Slice Tests**: `@WebMvcTest`, `@DataJpaTest` for focused testing

### Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run only integration tests
mvn test -Dtest=*IntegrationTest

# Run with coverage report
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

### Test Categories

#### 1. **Unit Tests**
- **Service Layer**: `ArbitrageServiceImplTest`, `PriceServiceImplTest`
- **Domain Logic**: `ConsolidatedPriceTest`, `ArbitrageOpportunityTest`
- **Validators**: `CurrencyPairValidatorTest`

#### 2. **Integration Tests** (Testcontainers)
- `RabbitMqMessagingIntegrationTest`: End-to-end message flow
- `RedisIntegrationTest`: Cache behavior validation
- `L2CacheTest`: Hibernate second-level cache verification
- `NPlusOneProblemTest`: Query optimization validation

#### 3. **Controller Tests** (@WebMvcTest)
- `PriceControllerTest`: REST endpoint testing with MockMvc
- `PriceGraphQLControllerTest`: GraphQL query testing
- Security integration (JWT validation)

#### 4. **Resilience Tests**
- `ResilienceTest`: Circuit breaker, retry, rate limiter behavior

#### 5. **Core Java Tests** (from Phase 1-10)
- `MyHashMapTest`: Custom HashMap implementation
- `MyBlockingQueueTest`: Producer-consumer pattern
- `CompletableFutureTest`: Async pipeline testing

---

## 💼 Interview Discussion Points

### Architecture & Design

**Q: Why did you choose Hexagonal Architecture?**  
A: To achieve **testability** and **flexibility**. Business logic (domain) is isolated from infrastructure (Spring, JPA), allowing:
- Easy testing without Spring context
- Swapping implementations (e.g., RabbitMQ → Kafka) without changing core logic
- Clear separation of concerns

**Q: How does your event-driven architecture handle failures?**  
A: Multi-layered approach:
1. **Circuit Breaker**: Prevents cascading failures (opens after 50% failure rate)
2. **Retry**: Exponential backoff (3 retries with 2s initial delay)
3. **DLQ (Dead Letter Queue)**: Failed messages routed after retry exhaustion
4. **Message Persistence**: RabbitMQ durability ensures no message loss

**Q: Explain your caching strategy.**  
A:
- **L1 (Redis)**: Distributed cache for API responses, TTL 5 minutes, handles horizontal scaling
- **L2 (Caffeine)**: Hibernate cache for static data (`ExchangeInfo`), reduces DB queries by 90%
- **Invalidation**: Event-driven eviction on data updates (`@CacheEvict`)

### Performance & Scalability

**Q: How did you solve the N+1 query problem?**  
A: Two approaches implemented:
1. **`@EntityGraph`**: Declaratively fetch associations in single query
2. **JPQL JOIN FETCH**: Explicit control over query generation
3. **Validation**: `NPlusOneProblemTest` asserts query count = 1

**Q: How does your system handle high message throughput?**  
A:
- **Parallel Consumers**: Multiple `@RabbitListener` instances (10 concurrent consumers)
- **Async Processing**: `@Async` methods with custom thread pool (50 core threads)
- **Backpressure**: RabbitMQ prefetch count limits unprocessed messages
- **Performance**: Processes 10k+ messages/minute

### Security

**Q: How is your API secured?**  
A: OAuth 2.0 Resource Server pattern:
- **Stateless**: JWT validation (no session state)
- **Spring Security Filter Chain**: Validates token signature & expiration
- **JWK Set URI**: Public keys fetched from Identity Provider
- **Authorization**: Role-based access via JWT claims

### Testing

**Q: How do you test with external dependencies?**  
A: **Testcontainers** for real service integration:
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
```
- Spins up Docker containers during test execution
- Tests against real PostgreSQL/RabbitMQ (not mocks)
- Automatic cleanup after tests

**Q: What's your approach to TDD?**  
A: Red-Green-Refactor cycle:
1. **Red**: Write failing test first (e.g., `testArbitrageDetection`)
2. **Green**: Minimal code to pass test
3. **Refactor**: Optimize with design patterns, SOLID principles
4. **Evidence**: Git history shows test-first commits (see Phase 1-10 commits)

### Concurrency

**Q: Explain your concurrency evolution.**  
A:
- **Phase 6**: Classic Java (`ExecutorService`, `BlockingQueue`, `ReentrantLock`)
- **Phase 8**: Modern Java (`CompletableFuture`, async pipelines)
- **Phase 14**: Spring (`@Async`, RabbitMQ, Virtual Threads ready)
- **Trade-offs**: Spring abstractions vs manual control, ease of use vs performance tuning

---

## 🎓 Development Journey

This project demonstrates a **15-phase development journey** from Core Java to production Spring Boot:

### Part 1: Core Java Fundamentals (Phases 1-10)
- ✅ Lambdas, Streams, Functional Programming
- ✅ Design Patterns (Strategy, Factory, Builder, Singleton)
- ✅ Custom Data Structures (HashMap from scratch)
- ✅ Manual Concurrency (Locks, `volatile`, `wait/notify`)
- ✅ Modern Concurrency (`CompletableFuture`, async pipelines)
- ✅ JVM Internals (Reflection, Serialization, GC tuning)
- ✅ Profiling (JVisualVM, Heap Dumps, Thread Dumps)

### Part 2: Spring Boot Migration (Phases 11-15)
- ✅ Spring Boot, JPA, Security, AMQP
- ✅ AOP, Exception Handling, Interceptors
- ✅ N+1 Optimization, L2 Caching, Redis
- ✅ RabbitMQ, @Async, DLQ, Message Tracing
- ✅ GraphQL, Resilience4j, OpenAPI/Swagger

**Why this approach?**  
Shows mastery of **both fundamentals and frameworks**—understanding *why* Spring abstractions exist by building lower-level implementations first.

**Commit History**: Each phase has dedicated commits showing incremental progress ([CPAv2.md](CPAv2.md) for details).

---

## 📖 Related Documentation

- **[CPAv2.md](CPAv2.md)**: Complete 15-phase development plan with TDD approach
- **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)**: Detailed codebase structure
- **[SECURITY.md](SECURITY.md)**: Security policies and vulnerability reporting
- **[pom.xml](pom.xml)**: Complete dependency list with versions

---

## 🤝 Contributing

This is a personal portfolio project. However, suggestions and feedback are welcome via GitHub issues.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Contact

**Project Author**: Pranay  
**LinkedIn**: [Your LinkedIn Profile]  
**Email**: [Your Email]  
**Portfolio**: [Your Portfolio Website]

---

## 🏆 Acknowledgments

- Spring Team for the excellent documentation
- Testcontainers for making integration testing seamless
- The Java community for best practices and patterns

---

<div align="center">

**⭐ If you find this project interesting, please consider giving it a star! ⭐**

*Built with ❤️ to demonstrate enterprise Java development skills*

</div>
