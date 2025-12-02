# Phase 6: External Decoupling with RabbitMQ (Producer/Consumer)

## Goal
Migrate the internal event-driven architecture (Phase 5) to an external message broker (RabbitMQ) to enable distributed processing, durability, and better scalability.

## Requirements

1.  **RabbitMQ Dependencies**: Add `spring-boot-starter-amqp` and `spring-rabbit-test`.
2.  **Configuration**: Set up RabbitMQ Exchange (`crypto.prices.exchange`) and Queue (`crypto.prices.queue`) with appropriate bindings.
3.  **Manual Producer (Education)**: Implement a `ManualPriceMessageProducer` using raw `ConnectionFactory` and `Channel` to demonstrate the low-level boilerplate (the "pain").
    *   *Constraint*: This should be visible but not active by default (use `@Profile("manual")`).
4.  **Professional Producer**: Implement a `PriceMessageProducer` using Spring's `RabbitTemplate` for production use.
    *   *Constraint*: Use dynamic routing keys (e.g., `prices.tick.binance`).
5.  **Refactor Consumer**: Convert `PriceTickConsumer` from an `@EventListener` to a `@RabbitListener`.
6.  **Service Layer Updates**: Ensure `PriceService` aggregates prices correctly despite the asynchronous nature of persistence (use return values from fetchers, not DB queries).
7.  **Docker Support**: Add `Dockerfile` and `docker-compose.yml` to run RabbitMQ and the application.
8.  **Testing**: Update integration tests to mock RabbitMQ and verify the async flow.
