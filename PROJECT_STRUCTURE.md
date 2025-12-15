# Project Structure

**Generated on:** December 15, 2025 at 01:03 PM

```text
.
├── 📦 .gitattributes
├── 🔵 .github
│   ├── 🔵 workflows
│   │   ├── ⚙️ deploy.yml
│   │   ├── ⚙️ maven.yml
├── 📦 .gitignore
├── 🔵 .mvn
│   ├── 🔵 wrapper
│   │   ├── ⚙️ maven-wrapper.properties
├── 📄 DEPLOYMENT.md
├── 📦 Dockerfile
├── 📄 README.md
├── ⚙️ docker-compose.yml
├── 🔵 frontend
│   ├── 📄 README.md
│   ├── 📦 app.js
│   ├── 📦 config.example.js
│   ├── 📦 index.html
│   ├── 📦 styles.css
│   ├── 📄 walkthrough.md
├── 📄 implementation_ngrok.md
├── 🔵 implementatonDocs
│   ├── 🔵 phase1
│   │   ├── 📄 PHASE_1_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase1.md
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase10
│   │   ├── 📦 implementation_plan.md.resolved
│   │   ├── 📦 task.md.resolved
│   │   ├── 📦 walkthrough.md.resolved
│   ├── 🔵 phase2
│   │   ├── 📄 PHASE_2_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase2.md
│   │   ├── 📄 task.md
│   ├── 🔵 phase3
│   │   ├── 📄 PHASE_3_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase3.md
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   │   ├── 📄 walkthrough2.md
│   ├── 🔵 phase4
│   │   ├── 📄 PROJECT_STRUCTURE_phase4.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase5
│   │   ├── 📄 PHASE_5_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase6.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase6
│   │   ├── 📄 PROJECT_STRUCTURE_phase6.md
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 phase_6_summary.md
│   │   ├── 📄 tasks.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase7
│   │   ├── 📄 PROJECT_STRUCTURE_phase7.md
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 phase_7_summary.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase8
│   │   ├── 📄 PHASE_8_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase8.md
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
│   ├── 🔵 phase9
│   │   ├── 📄 PHASE_9_SUMMARY.md
│   │   ├── 📄 PROJECT_STRUCTURE_phase9.md
│   │   ├── 📄 implementation_plan.md
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
├── 📦 mvnw
├── 📜 mvnw.cmd
├── ⚙️ pom.xml
├── 🔵 src
│   ├── 🔵 main
│   │   ├── 🔵 java
│   │   │   ├── 🔵 com
│   │   │   │   ├── 🔵 cryptoArb
│   │   │   │   │   ├── 🔵 crypto_price_aggregator
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplication.java
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
│   │   │   │   │   │   │   ├── ☕ WebConfig.java
│   │   │   │   │   │   │   ├── ☕ WebSocketConfig.java
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ ArbitrageController.java
│   │   │   │   │   │   │   ├── ☕ PriceController.java
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
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngine.java
│   │   │   │   │   │   │   ├── 📄 ManualConcurrentPriceEngine.md
│   │   │   │   │   │   │   ├── ☕ ManualPriceMessageProducer.java
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceMessageProducer.java
│   │   │   │   │   │   │   ├── ☕ PriceService.java
│   │   │   │   │   │   │   ├── ☕ PriceTickConsumer.java
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ ArbitrageServiceImpl.java
│   │   │   │   │   │   │   │   ├── ☕ BinanceFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ CoinbaseFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ KrakenFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ ManualResilientBinanceFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ PriceServiceImpl.java
│   │   │   │   │   │   │   │   ├── 📄 PriceServiceImpl.md
│   │   │   │   │   │   ├── 🔵 validation
│   │   │   │   │   │   │   ├── ☕ CurrencyPairValidator.java
│   │   │   │   │   │   │   ├── ☕ ValidCurrencyPair.java
│   │   │   │   │   │   ├── 🔵 websocket
│   │   │   │   │   │   │   ├── ☕ ExchangeWebSocketClient.java
│   │   │   │   │   │   │   ├── ☕ WebSocketRateLimiter.java
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ MockWebSocketClient.java
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
│   │   │   │   │   │   ├── ☕ BaseIntegrationTest.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplicationTests.java
│   │   │   │   │   │   ├── ☕ FlywayMigrationTest.java
│   │   │   │   │   │   ├── ☕ PostgreSQLIntegrationTest.java
│   │   │   │   │   │   ├── 🔵 aspect
│   │   │   │   │   │   │   ├── ☕ LoggingAspectTest.java
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
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ ArbitrageOpportunityTest.java
│   │   │   │   │   │   │   ├── ☕ CurrencyPairTest.java
│   │   │   │   │   │   │   ├── ☕ ExchangeTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickTest.java
│   │   │   │   │   │   ├── 🔵 filter
│   │   │   │   │   │   │   ├── ☕ RequestLoggingFilterTest.java
│   │   │   │   │   │   ├── 📄 flywayMigrationTest_walkthrough.md
│   │   │   │   │   │   ├── 🔵 health
│   │   │   │   │   │   │   ├── ☕ ExchangeHealthIndicatorTest.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ ArbitrageRepositoryTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickRepositoryTest.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── ☕ ArbitrageServiceTest.java
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngineTest.java
│   │   │   │   │   │   │   ├── ☕ ManualFetcherIntegrationTest.java
│   │   │   │   │   │   │   ├── ☕ ManualPriceMessageProducerTest.java
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcherTest.java
│   │   │   │   │   │   │   ├── ☕ PriceMessageProducerTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceGetLatestTicksTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceIntegrationTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickConsumerTest.java
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

**Total Files:** 159

**Total Directories:** 61
