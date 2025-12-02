# Project Structure

**Generated on:** December 02, 2025 at 10:17 PM

```text
.
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
│   │   ├── 📄 walkthrough.md
├── 🔵 .mvn
├── 📦 mvnw
├── 📜 mvnw.cmd
│   ├── 🔵 wrapper
│   │   ├── ⚙️ maven-wrapper.properties
├── ⚙️ pom.xml
├── 🔵 .qodo
│   ├── 🔵 agents
│   ├── 🔵 workflows
├── 📄 README.md
├── 🔵 src
│   ├── 🔵 main
│   │   ├── 🔵 java
│   │   │   ├── 🔵 com
│   │   │   │   ├── 🔵 cryptoArb
│   │   │   │   │   ├── 🔵 crypto_price_aggregator
│   │   │   │   │   │   ├── 🔵 benchmark
│   │   │   │   │   │   │   ├── ☕ BenchmarkRunner.java
│   │   │   │   │   │   │   ├── 📄 BenchmarkRunner.md
│   │   │   │   │   │   ├── 🔵 concurrency
│   │   │   │   │   │   │   ├── ☕ MyBlockingQueue.java
│   │   │   │   │   │   │   ├── ☕ VolatileFlagStop.java
│   │   │   │   │   │   ├── 🔵 config
│   │   │   │   │   │   │   ├── ☕ AppConfig.java
│   │   │   │   │   │   │   ├── ☕ JpaConfig.java
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
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ PriceTickRepository.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ BinanceFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ CoinbaseFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ KrakenFetcher.java
│   │   │   │   │   │   │   │   ├── ☕ PriceServiceImpl.java
│   │   │   │   │   │   │   │   ├── 📄 PriceServiceImpl.md
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngine.java
│   │   │   │   │   │   │   ├── 📄 ManualConcurrentPriceEngine.md
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceFetcher.java
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
│   │   │   │   │   │   ├── 🔵 benchmark
│   │   │   │   │   │   │   ├── ☕ BenchmarkRunnerTest.java
│   │   │   │   │   │   │   ├── 📄 BenchmarkTest.md
│   │   │   │   │   │   ├── 🔵 concurrency
│   │   │   │   │   │   │   ├── ☕ MyBlockingQueueTest.java
│   │   │   │   │   │   │   ├── ☕ VolatileFlagStopTest.java
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ PriceControllerGetExchangesTest.java
│   │   │   │   │   │   │   ├── ☕ PriceControllerTest.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplicationTests.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ CurrencyPairTest.java
│   │   │   │   │   │   │   ├── ☕ ExchangeTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickTest.java
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
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngineTest.java
│   │   │   │   │   │   │   ├── ☕ ManualFetcherIntegrationTest.java
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcherTest.java
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

**Total Files:** 78

**Total Directories:** 44
