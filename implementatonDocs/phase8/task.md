# Phase 8: Gradual Observability (Logging, Metrics)

## Task Breakdown

### Planning
- [x] Create task breakdown
- [x] Create implementation plan
- [x] Get user approval for implementation plan

### Implementation - Manual MDC for Request IDs
- [x] RED: Write failing test for `RequestLoggingFilter`
- [x] GREEN: Implement `RequestLoggingFilter` with manual MDC setup
- [x] REFACTOR: Ensure filter is registered in Spring boot
- [x] Verify correlation IDs appear in logs

### Implementation - Custom Health Indicators
- [x] RED: Write failing test for `ExchangeHealthIndicator`
- [x] GREEN: Implement `ExchangeHealthIndicator` for fetchers
- [x] REFACTOR: Register health indicator as bean
- [x] Verify health endpoint shows exchange status

### Implementation - Metrics with Micrometer
- [x] RED: Write failing test for `MetricsConfig`
- [x] GREEN: Implement `MetricsConfig` with custom metrics
- [ ] Add `@Timed` annotations to service methods
- [ ] Add counters in producers/consumers
- [ ] REFACTOR: Verify metrics appear in actuator

### Implementation - Logging Aspect
- [x] RED: Write failing test for `LoggingAspect`
- [x] GREEN: Implement `LoggingAspect` for method entry/exit
- [x] REFACTOR: Configure aspect for service layer
- [x] Verify aspect logs appear

### Configuration & Dependencies
- [x] Update `pom.xml` with Micrometer dependencies
- [x] Update `application.properties` with actuator/metrics config
- [x] Enable additional actuator endpoints

### Testing & Verification
- [x] Run all unit tests
- [x] Run integration tests
- [ ] Manual verification via actuator endpoints
- [ ] Check logs for correlation IDs and aspect logs
- [ ] Update project structure documentation

### Documentation
- [/] Create walkthrough document
- [ ] Update phase 8 summary
