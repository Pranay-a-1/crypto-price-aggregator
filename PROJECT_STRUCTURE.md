# Project Structure

**Generated on:** December 15, 2025 at 12:40 AM

```text
.
├── ⚙️ docker-compose.yml
├── 📦 Dockerfile
├── 📦 .gitattributes
├── 🔵 .github
│   ├── 🔵 workflows
│   │   ├── ⚙️ maven.yml
├── 📦 .gitignore
├── 🔵 implementatonDocs
│   ├── 🔵 phase1
│   ├── 🔵 phase10
│   │   ├── 📦 implementation_plan.md.resolved
│   │   ├── 📦 task.md.resolved
│   │   ├── 📦 walkthrough.md.resolved
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 PHASE_1_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase1.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase2
│   │   ├── 📄 PHASE_2_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase2.md
│   │   ├── 📄 task.md
│   ├── 🔵 phase3
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 PHASE_3_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase3.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough2.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase4
│   │   ├── 📄 PROJECT_STRUCTURE_phase4.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase5
│   │   ├── 📄 PHASE_5_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase6.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase6
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 phase_6_summary.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase6.md
│   │   ├── 📄 tasks.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase7
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 phase_7_summary.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase7.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase8
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 PHASE_8_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase8.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase9
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 PHASE_9_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase9.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
├── 🔵 .mvn
├── 📦 mvnw
├── 📜 mvnw.cmd
│   ├── 🔵 wrapper
│   │   ├── ⚙️ maven-wrapper.properties
├── ⚙️ pom.xml
├── ⚙️ qodana.yaml
├── 📄 README.md
├── 🔵 src
│   ├── 🔵 main
│   │   ├── 🔵 java
│   │   │   ├── 🔵 com
│   │   │   │   ├── 🔵 cryptoArb
│   │   │   │   │   ├── 🔵 crypto_price_aggregator
│   │   │   │   │   │   ├── 🔵 aspect
│   │   │   │   │   │   │   ├── ☕ LoggingAspect.java
│   │   │   │   │   │   ├── 🔵 benchmark
│   │   │   │   │   │   │   ├── ☕ BenchmarkRunner.java
│   │   │   │   │   │   │   ├── 📄 BenchmarkRunner.md
│   │   │   │   │   │   ├── 🔵 concurrency
│   │   │   │   │   │   │   ├── ☕ MyBlockingQueue.java
│   │   │   │   │   │   │   ├── ☕ VolatileFlagStop.java
│   │   │   │   │   │   ├── 🔵 config
│   │   │   │   │   │   │   ├── ☕ AppConfig.java
│   │   │   │   │   │   │   ├── ☕ JpaConfig.java
│   │   │   │   │   │   │   ├── ☕ MetricsConfig.java
│   │   │   │   │   │   │   ├── ☕ RabbitMqConfig.java
│   │   │   │   │   │   │   ├── ☕ SecurityConfig.java
│   │   │   │   │   │   │   ├── ☕ WebSocketConfig.java
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ ArbitrageController.java
│   │   │   │   │   │   │   ├── ☕ PriceController.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplication.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ AggregatedTopOfBookQuote.java
│   │   │   │   │   │   │   ├── ☕ ArbitrageOpportunity.java
│   │   │   │   │   │   │   ├── ☕ CurrencyPair.java
│   │   │   │   │   │   │   ├── ☕ Exchange.java
│   │   │   │   │   │   │   ├── ☕ PriceTick.java
│   │   │   │   │   │   ├── 🔵 event
│   │   │   │   │   │   │   ├── ☕ PriceTickFetchedEvent.java
│   │   │   │   │   │   ├── 🔵 exception
│   │   │   │   │   │   │   ├── ☕ GlobalExceptionHandler.java
│   │   │   │   │   │   │   ├── ☕ PriceFetchException.java
│   │   │   │   │   │   ├── 🔵 filter
│   │   │   │   │   │   │   ├── ☕ RequestLoggingFilter.java
│   │   │   │   │   │   ├── 🔵 health
│   │   │   │   │   │   │   ├── ☕ ExchangeHealthIndicator.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ ArbitrageRepository.java
│   │   │   │   │   │   │   ├── ☕ PriceTickRepository.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── ☕ ArbitrageService.java
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ ArbitrageServiceImpl.java
│   │   │   │   │   │   │   │   ├── ☕ BinanceFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ CoinbaseFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ KrakenFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ ManualResilientBinanceFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ PriceServiceImpl.java
│   │   │   │   │   │   │   │   ├── 📄 PriceServiceImpl.md
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngine.java
│   │   │   │   │   │   │   ├── 📄 ManualConcurrentPriceEngine.md
│   │   │   │   │   │   │   ├── ☕ ManualPriceMessageProducer.java
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceMessageProducer.java
│   │   │   │   │   │   │   ├── ☕ PriceService.java
│   │   │   │   │   │   │   ├── ☕ PriceTickConsumer.java
│   │   │   │   │   │   ├── 🔵 validation
│   │   │   │   │   │   │   ├── ☕ CurrencyPairValidator.java
│   │   │   │   │   │   │   ├── ☕ ValidCurrencyPair.java
│   │   │   │   │   │   ├── 🔵 websocket
│   │   │   │   │   │   │   ├── ☕ ExchangeWebSocketClient.java
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ MockWebSocketClient.java
│   │   │   │   │   │   │   ├── ☕ WebSocketRateLimiter.java
│   │   ├── 🔵 resources
│   │   │   ├── ⚙️ application-dev.properties
│   │   │   ├── ⚙️ application-prod.properties
│   │   │   ├── ⚙️ application.properties
│   │   │   ├── 🔵 db
│   │   │   │   ├── 🔵 migration
│   │   │   │   │   ├── 📦 V1__initial_schema.sql
│   │   │   │   │   ├── 📦 V2__create_arbitrage_opportunities.sql
│   ├── 🔵 test
│   │   ├── 🔵 java
│   │   │   ├── 🔵 com
│   │   │   │   ├── 🔵 cryptoArb
│   │   │   │   │   ├── 🔵 crypto_price_aggregator
│   │   │   │   │   │   ├── 🔵 aspect
│   │   │   │   │   │   │   ├── ☕ LoggingAspectTest.java
│   │   │   │   │   │   ├── ☕ BaseIntegrationTest.java
│   │   │   │   │   │   ├── 🔵 benchmark
│   │   │   │   │   │   │   ├── ☕ BenchmarkRunnerTest.java
│   │   │   │   │   │   │   ├── 📄 BenchmarkTest.md
│   │   │   │   │   │   ├── 🔵 concurrency
│   │   │   │   │   │   │   ├── ☕ MyBlockingQueueTest.java
│   │   │   │   │   │   │   ├── ☕ VolatileFlagStopTest.java
│   │   │   │   │   │   ├── 🔵 config
│   │   │   │   │   │   │   ├── ☕ MetricsConfigTest.java
│   │   │   │   │   │   │   ├── ☕ RabbitMqConfigTest.java
│   │   │   │   │   │   │   ├── ☕ SecurityConfigTest.java
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ ArbitrageControllerTest.java
│   │   │   │   │   │   │   ├── ☕ PriceControllerGetExchangesTest.java
│   │   │   │   │   │   │   ├── ☕ PriceControllerTest.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplicationTests.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ ArbitrageOpportunityTest.java
│   │   │   │   │   │   │   ├── ☕ CurrencyPairTest.java
│   │   │   │   │   │   │   ├── ☕ ExchangeTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickTest.java
│   │   │   │   │   │   ├── 🔵 filter
│   │   │   │   │   │   │   ├── ☕ RequestLoggingFilterTest.java
│   │   │   │   │   │   ├── ☕ FlywayMigrationTest.java
│   │   │   │   │   │   ├── 📄 flywayMigrationTest_walkthrough.md
│   │   │   │   │   │   ├── 🔵 health
│   │   │   │   │   │   │   ├── ☕ ExchangeHealthIndicatorTest.java
│   │   │   │   │   │   ├── ☕ PostgreSQLIntegrationTest.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ ArbitrageRepositoryTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickRepositoryTest.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── ☕ ArbitrageServiceTest.java
│   │   │   │   │   │   │   ├── 🔵 docs
│   │   │   │   │   │   │   │   ├── 📄 COINBASE_TESTING_GUIDE.md
│   │   │   │   │   │   │   │   ├── 📄 ManualConcurrentPriceEngineTest.md
│   │   │   │   │   │   │   │   ├── 📄 PriceServiceTest.md
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ BinanceFetcherSymbolTest.java
│   │   │   │   │   │   │   │   ├── ☕ BinanceFetcherTest.java
│   │   │   │   │   │   │   │   ├── ☕ CoinbaseFetcherTest.java
│   │   │   │   │   │   │   │   ├── ☕ KrakenFetcherTest.java
│   │   │   │   │   │   │   │   ├── ☕ ManualResilientBinanceFetcherTest.java
│   │   │   │   │   │   │   │   ├── ☕ ResilienceIntegrationTest.java
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngineTest.java
│   │   │   │   │   │   │   ├── ☕ ManualFetcherIntegrationTest.java
│   │   │   │   │   │   │   ├── ☕ ManualPriceMessageProducerTest.java
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcherTest.java
│   │   │   │   │   │   │   ├── ☕ PriceMessageProducerTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceGetLatestTicksTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceIntegrationTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickConsumerTest.java
│   │   │   │   │   │   ├── 🔵 validation
│   │   │   │   │   │   │   ├── ☕ CurrencyPairValidatorTest.java
│   │   ├── 🔵 resources
│   │   │   ├── ⚙️ application-test.properties
```

---

### Legend

- 🔵 Directories
- ☕ Java source files
- ⚙️ Configuration files
- 📜 Scripts
- 📄 Documentation
- 📦 Other files

---

**Total Files:** 150

**Total Directories:** 60
