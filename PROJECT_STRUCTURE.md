# Project Structure

**Generated on:** November 28, 2025 at 06:39 PM

```text
.
├── 📦 .gitattributes
├── 📦 .gitignore
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
│   │   │   │   │   │   ├── 🔵 config
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
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceFetcher.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceImpl.java
│   │   │   │   │   │   │   ├── ☕ PriceService.java
│   │   ├── 🔵 resources
│   │   │   ├── ⚙️ application.properties
│   │   │   ├── 🔵 static
│   │   │   ├── 🔵 templates
│   ├── 🔵 test
│   │   ├── 🔵 java
│   │   │   ├── 🔵 com
│   │   │   │   ├── 🔵 cryptoArb
│   │   │   │   │   ├── 🔵 crypto_price_aggregator
│   │   │   │   │   │   ├── 🔵 controller
│   │   │   │   │   │   │   ├── ☕ PriceControllerTest.java
│   │   │   │   │   │   ├── ☕ CryptoPriceAggregatorApplicationTests.java
│   │   │   │   │   │   ├── 🔵 domain
│   │   │   │   │   │   │   ├── ☕ CurrencyPairTest.java
│   │   │   │   │   │   │   ├── ☕ ExchangeTest.java
│   │   │   │   │   │   │   ├── ☕ PriceTickTest.java
│   │   │   │   │   │   ├── 🔵 service
│   │   │   │   │   │   │   ├── ☕ MockPriceFetcherTest.java
│   │   │   │   │   │   │   ├── ☕ PriceServiceTest.java
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

**Total Files:** 27

**Total Directories:** 25
