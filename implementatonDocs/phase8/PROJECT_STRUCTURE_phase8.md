# Project Structure

**Generated on:** December 04, 2025 at 01:21 AM

```text
.
├── ⚙️ docker-compose.yml
├── 📦 Dockerfile
├── 📦 .gitattributes
├── 📦 .gitignore
├── 🔵 implementatonDocs
│   ├── 🔵 phase1
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
├── 🔵 .mvn
├── 📦 mvnw
├── 📜 mvnw.cmd
│   ├── 🔵 wrapper
│   │   ├── ⚙️ maven-wrapper.properties
├── ⚙️ pom.xml
├── 🔵 .qodo
│   ├── 🔵 agents
│   ├── 🔵 workflows
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
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ PriceController.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplication.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ AggregatedTopOfBookQuote.java
│   │   │   │   │   │   │   ├── ☕ CurrencyPair.java
│   │   │   │   │   │   │   ├── ☕ Exchange.java
│   │   │   │   │   │   │   ├── ☕ PriceTick.java
│   │   │   │   │   │   ├── 🔵 event
│   │   │   │   │   │   │   ├── ☕ PriceTickFetchedEvent.java
│   │   │   │   │   │   ├── 🔵 exception
│   │   │   │   │   │   │   ├── ☕ PriceFetchException.java
│   │   │   │   │   │   ├── 🔵 filter
│   │   │   │   │   │   │   ├── ☕ RequestLoggingFilter.java
│   │   │   │   │   │   ├── 🔵 health
│   │   │   │   │   │   │   ├── ☕ ExchangeHealthIndicator.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ PriceTickRepository.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── 🔵 impl
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
│   │   ├── 🔵 resources
│   │   │   ├── ⚙️ application-dev.properties
│   │   │   ├── ⚙️ application.properties
│   │   │   ├── 🔵 static
│   │   │   ├── 🔵 templates
│   ├── 🔵 test
│   │   ├── 🔵 java
│   │   │   ├── 🔵 com
│   │   │   │   ├── 🔵 cryptoArb
│   │   │   │   │   ├── 🔵 crypto_price_aggregator
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
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ PriceControllerGetExchangesTest.java
│   │   │   │   │   │   │   ├── ☕ PriceControllerTest.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplicationTests.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ CurrencyPairTest.java
│   │   │   │   │   │   │   ├── ☕ ExchangeTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickTest.java
│   │   │   │   │   │   ├── 🔵 filter
│   │   │   │   │   │   │   ├── ☕ RequestLoggingFilterTest.java
│   │   │   │   │   │   ├── 🔵 health
│   │   │   │   │   │   │   ├── ☕ ExchangeHealthIndicatorTest.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ PriceTickRepositoryTest.java
│   │   │   │   │   │   ├── 🔵 service
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

**Total Files:** 107

**Total Directories:** 54
