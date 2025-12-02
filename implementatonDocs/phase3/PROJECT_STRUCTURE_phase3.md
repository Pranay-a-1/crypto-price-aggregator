# Project Structure

**Generated on:** December 02, 2025 at 02:29 PM

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
│   │   ├── 📄 task.md
│   │   ├── 📄 walkthrough.md
├── 🔵 .mvn
├── 📦 mvnw
├── 📜 mvnw.cmd
│   ├── 🔵 wrapper
│   │   ├── ⚙️ maven-wrapper.properties
├── ⚙️ pom.xml
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
│   │   │   │   │   │   │   ├── ☕ JpaConfig.java
│   │   │   │   │   │   │   ├── ☕ PriceFetcherConfig.java
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ PriceController.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplication.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ AggregatedTopOfBookQuote.java
│   │   │   │   │   │   │   ├── ☕ CurrencyPair.java
│   │   │   │   │   │   │   ├── ☕ Exchange.java
│   │   │   │   │   │   │   ├── ☕ PriceTick.java
│   │   │   │   │   │   ├── 🔵 exception
│   │   │   │   │   │   │   ├── ☕ PriceFetchException.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ PriceTickRepository.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── 🔵 impl
│   │   │   │   │   │   │   │   ├── ☕ PriceServiceImpl.java
│   │   │   │   │   │   │   │   ├── 📄 PriceServiceImpl.md
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngine.java
│   │   │   │   │   │   │   ├── 📄 ManualConcurrentPriceEngine.md
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceService.java
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
│   │   │   │   │   │   │   ├── ☕ PriceControllerTest.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplicationTests.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ CurrencyPairTest.java
│   │   │   │   │   │   │   ├── ☕ ExchangeTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickTest.java
│   │   │   │   │   │   ├── 🔵 repository
│   │   │   │   │   │   │   ├── ☕ PriceTickRepositoryTest.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── ☕ ManualConcurrentPriceEngineTest.java
│   │   │   │   │   │   │   ├── 📄 ManualConcurrentPriceEngineTest.md
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcherTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceTest.java
│   │   │   │   │   │   │   ├── 📄 PriceServiceTest.md
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

**Total Files:** 57

**Total Directories:** 36
