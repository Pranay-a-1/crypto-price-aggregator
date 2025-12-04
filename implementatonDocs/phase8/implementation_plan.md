# Phase 8: Gradual Observability (Logging, Metrics)

This phase implements observability features following the TDD principle and building from manual implementations to Spring's sophisticated abstractions.

## User Review Required

> [!IMPORTANT]
> **Testing Strategy**: All components will be tested using JUnit 5 and Mockito. Integration tests will verify actuator endpoints and metric collection.

> [!IMPORTANT]
> **Dependencies**: We'll add Micrometer registry dependencies to `pom.xml`. The actuator dependency is already present.

## Proposed Changes

### Observability Components

#### [NEW] [RequestLoggingFilter.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/filter/RequestLoggingFilter.java)

**Purpose**: Add correlation IDs to all requests for distributed tracing

**Implementation**:
- Implement `Filter` interface (manual approach)
- Generate unique request ID using `UUID.randomUUID()`
- Add request ID to MDC (Mapped Diagnostic Context) using `MDC.put("requestId", ...)`
- Log incoming requests with correlation ID
- Clean up MDC in `finally` block with `MDC.clear()`
- Filter will execute for all requests (`/*` pattern)

**Why Manual First**: Direct MDC manipulation shows how correlation IDs work before using Spring's abstractions.

---

#### [NEW] [ExchangeHealthIndicator.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/health/ExchangeHealthIndicator.java)

**Purpose**: Custom health check for exchange fetchers

**Implementation**:
- Implement Spring Boot's `HealthIndicator` interface
- Inject list of `PriceFetcher` beans
- Override `health()` method to check each exchange availability
- Perform lightweight health check (e.g., ping endpoint or check last successful fetch timestamp)
- Return `Health.up()` if all exchanges are reachable, `Health.down()` otherwise
- Include details about each exchange status

**Professional Refactor**: Use `@Component` annotation and Spring's dependency injection.

---

#### [NEW] [MetricsConfig.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/config/MetricsConfig.java)

**Purpose**: Configure custom Micrometer metrics

**Implementation**:
- Use `@Configuration` annotation
- Inject `MeterRegistry` bean
- Create custom counters for:
  - `price.fetch.success` - successful price fetches per exchange
  - `price.fetch.failure` - failed price fetches per exchange
  - `price.aggregation.total` - total aggregations performed
- Create custom timers for:
  - `price.fetch.duration` - time taken to fetch prices
- Expose these metrics as beans for injection into services

**Why Professional**: Micrometer is the standard metrics library in Spring Boot ecosystem.

---

#### [NEW] [LoggingAspect.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/aspect/LoggingAspect.java)

**Purpose**: AOP-based logging for service layer methods

**Implementation**:
- Use `@Aspect` and `@Component` annotations
- Define pointcut for all service layer methods: `@Around("execution(* com.cryptoArb.crypto_price_aggregator.service..*(..))")`
- Log method entry with parameters
- Log method exit with return value
- Log execution time
- Handle exceptions and log them

**Why AOP**: Aspect-oriented programming separates cross-cutting concerns (logging) from business logic.

---

### Service Layer Updates

#### [MODIFY] [PriceServiceImpl.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/PriceServiceImpl.java)

**Changes**:
- Add `@Timed("price.aggregation")` annotation to `getAggregatedTopOfBookQuoteForPair()` method
- Inject `MeterRegistry` for custom counters
- Increment `price.aggregation.total` counter on each call
- Add try-catch to track success/failure metrics

---

#### [MODIFY] [BinanceFetcher.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/BinanceFetcher.java)

**Changes**:
- Add `@Timed("price.fetch.binance")` annotation to `fetchPrice()` method
- Inject `MeterRegistry`
- Increment success/failure counters based on fetch result

---

#### [MODIFY] [CoinbaseFetcher.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/CoinbaseFetcher.java)

**Changes**:
- Add `@Timed("price.fetch.coinbase")` annotation to `fetchPrice()` method
- Inject `MeterRegistry`
- Increment success/failure counters based on fetch result

---

#### [MODIFY] [KrakenFetcher.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/impl/KrakenFetcher.java)

**Changes**:
- Add `@Timed("price.fetch.kraken")` annotation to `fetchPrice()` method
- Inject `MeterRegistry`
- Increment success/failure counters based on fetch result

---

#### [MODIFY] [PriceTickConsumer.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceTickConsumer.java)

**Changes**:
- Inject `MeterRegistry`
- Increment `price.tick.consumed` counter in message handler
- Add `@Timed("price.tick.processing")` annotation

---

#### [MODIFY] [PriceMessageProducer.java](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/java/com/cryptoArb/crypto_price_aggregator/service/PriceMessageProducer.java)

**Changes**:
- Inject `MeterRegistry`
- Increment `price.tick.produced` counter when publishing messages

---

### Configuration Updates

#### [MODIFY] [pom.xml](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/pom.xml)

**Changes**:
- Add Micrometer registry dependencies (already have actuator)
- Confirm AOP dependency is present (already added in Phase 7)

```xml
<!-- Micrometer Prometheus Registry (for metrics export) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

#### [MODIFY] [application.properties](file:///home/pranay/anotherDrive/javaCodes/crypto-price-aggregator/src/main/resources/application.properties)

**Changes**:
- Expose additional actuator endpoints: `metrics`, `prometheus`, `health`
- Configure health indicator details
- Enable correlation ID in logging pattern
- Configure aspect ordering

```properties
# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always

# Metrics
management.metrics.tags.application=${spring.application.name}
management.metrics.distribution.percentiles-histogram.http.server.requests=true

# Logging with Correlation ID
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - [%X{requestId}] [%thread] %-5level %logger{36} - %msg%n
```

## Verification Plan

### Automated Tests

#### Unit Tests

**1. RequestLoggingFilter Test**
```bash
# File: src/test/java/com/cryptoArb/crypto_price_aggregator/filter/RequestLoggingFilterTest.java
mvn test -Dtest=RequestLoggingFilterTest
```
- Verify filter adds request ID to MDC
- Verify filter clears MDC after request
- Verify logging includes correlation ID

**2. ExchangeHealthIndicator Test**
```bash
# File: src/test/java/com/cryptoArb/crypto_price_aggregator/health/ExchangeHealthIndicatorTest.java
mvn test -Dtest=ExchangeHealthIndicatorTest
```
- Verify health returns UP when all exchanges are healthy
- Verify health returns DOWN when any exchange fails
- Verify health details include individual exchange status

**3. MetricsConfig Test**
```bash
# File: src/test/java/com/cryptoArb/crypto_price_aggregator/config/MetricsConfigTest.java
mvn test -Dtest=MetricsConfigTest
```
- Verify custom counters are registered
- Verify custom timers are registered

**4. LoggingAspect Test**
```bash
# File: src/test/java/com/cryptoArb/crypto_price_aggregator/aspect/LoggingAspectTest.java
mvn test -Dtest=LoggingAspectTest
```
- Verify aspect logs method entry
- Verify aspect logs method exit
- Verify aspect logs execution time
- Verify aspect logs exceptions

#### Integration Tests

**5. Actuator Endpoints Integration Test**
```bash
# File: src/test/java/com/cryptoArb/crypto_price_aggregator/actuator/ActuatorEndpointsIntegrationTest.java
mvn test -Dtest=ActuatorEndpointsIntegrationTest
```
- Verify `/actuator/health` returns custom health indicator data
- Verify `/actuator/metrics` exposes custom metrics
- Verify `/actuator/prometheus` endpoint is accessible

**6. Metrics Collection Integration Test**
```bash
# File: src/test/java/com/cryptoArb/crypto_price_aggregator/metrics/MetricsIntegrationTest.java
mvn test -Dtest=MetricsIntegrationTest
```
- Call price service and verify metrics are incremented
- Verify @Timed metrics are recorded
- Verify counter metrics are incremented correctly

### Manual Verification

**1. Start application and check actuator endpoints**
```bash
# Start the application
mvn spring-boot:run

# In another terminal, check health endpoint
curl http://localhost:8080/actuator/health

# Check metrics endpoint
curl http://localhost:8080/actuator/metrics

# Check specific metric
curl http://localhost:8080/actuator/metrics/price.fetch.success

# Check prometheus endpoint
curl http://localhost:8080/actuator/prometheus
```

**2. Verify correlation IDs in logs**
```bash
# Make a request and observe logs
curl http://localhost:8080/api/prices/BTCUSDT

# Check logs for correlation ID [requestId=xxx]
# Should see entries like:
# 2024-12-04 01:00:00 - [a1b2c3d4-...] [http-nio-8080-exec-1] INFO  c.c.c.f.RequestLoggingFilter - Incoming request: GET /api/prices/BTCUSDT
```

**3. Verify aspect logging**
```bash
# Make request and check logs for aspect entries
curl http://localhost:8080/api/prices/BTCUSDT

# Should see method entry/exit logs:
# Entering method: getAggregatedTopOfBookQuoteForPair with args: [BTCUSDT]
# Exiting method: getAggregatedTopOfBookQuoteForPair with result: ...
# Execution time: 123ms
```

**4. Run all tests**
```bash
mvn clean test
```
