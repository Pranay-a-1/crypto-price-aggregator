# Phase 6 Implementation Plan

## 1. Dependencies and Configuration
-   [x] Add `spring-boot-starter-amqp` to `pom.xml`.
-   [x] Add `spring-rabbit-test` to `pom.xml` (test scope).
-   [x] Create `RabbitMqConfig` class:
    -   Define TopicExchange: `crypto.prices.exchange`.
    -   Define Queue: `crypto.prices.queue`.
    -   Define Binding: Queue -> Exchange with key `prices.tick.#`.
    -   Define `Jackson2JsonMessageConverter` for JSON serialization.

## 2. Manual Producer (Educational)
-   [x] Create `ManualPriceMessageProducer` implementing `ApplicationListener<PriceTickFetchedEvent>`.
-   [x] Inject `CachingConnectionFactory` but access the raw `Connection` and `Channel`.
-   [x] Manually publish JSON-serialized `PriceTick` objects.
-   [x] Restrict this bean to `@Profile("manual")` to prevent duplicate publishing in production.

## 3. Professional Producer
-   [x] Create `PriceMessageProducer` implementing `ApplicationListener<PriceTickFetchedEvent>`.
-   [x] Inject `RabbitTemplate`.
-   [x] Publish `PriceTick` objects using `convertAndSend` with dynamic routing keys (e.g., `prices.tick.<exchange>`).

## 4. Consumer Refactor
-   [x] Modify `PriceTickConsumer`.
-   [x] Replace `@EventListener` with `@RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)`.
-   [x] Ensure it accepts deserialized `PriceTick` objects and saves them to `PriceTickRepository`.

## 5. Service Layer Adaptation
-   [x] Update `PriceServiceImpl.getAggregatedTopOfBookQuote`.
-   [x] Instead of querying the repository (which now receives data asynchronously and might lag), aggregate the *fresh* ticks returned directly by `executionEngine.fetchPrices`.
-   [x] This resolves the read-after-write consistency issue inherent in async systems.

## 6. Docker Containerization
-   [x] Create `Dockerfile` (Multi-stage build: Maven Build -> JRE Run).
-   [x] Create `docker-compose.yml` defining:
    -   `rabbitmq` service (Management plugin enabled).
    -   `app` service (Builds from Dockerfile, depends on RabbitMQ).

## 7. Testing
-   [x] Create `TestRabbitConfig` to mock `ConnectionFactory` and `RabbitTemplate` during integration tests (since no real broker is present in the build env).
-   [x] Update `PriceServiceIntegrationTest`:
    -   Mock `RabbitTemplate.convertAndSend` to manually invoke the `PriceTickConsumer`.
    -   Adjust assertions to account for asynchronous behavior (wait/sleep).
    -   Verify the full flow: Fetch -> Event -> Producer -> (Mock MQ) -> Consumer -> DB.
