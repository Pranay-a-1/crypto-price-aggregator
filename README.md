# Crypto Price Aggregator (CPA)

![Java CI with Maven](https://github.com/Pranay-a-1/crypto-price-aggregator/actions/workflows/maven.yml/badge.svg)

A robust Java Spring Boot application designed to aggregate real-time cryptocurrency prices from multiple exchanges and identify profitable arbitrage opportunities.

## 🚀 Features

*   **Multi-Exchange Integration:** Fetches real-time market data from major exchanges including **Binance**, **Coinbase**, and **Kraken**.
*   **Arbitrage Detection:** Continuously analyzes price disparities across exchanges to detect and persist arbitrage opportunities.
*   **High Concurrency:** Utilizes concurrent fetching strategies and `ManualConcurrentPriceEngine` for optimized performance.
*   **Event-Driven Architecture:** Leverages **RabbitMQ** for asynchronous price tick processing and system decoupling.
*   **Real-Time Data:** Supports WebSocket connections for low-latency updates (with rate limiting).
*   **Persistence:** Stores market data and arbitrage events in **PostgreSQL**, with schema management handled by **Flyway**.
*   **Resilience & Monitoring:** Includes custom health indicators, metrics configuration, and fault-tolerant fetching logic.
*   **Dockerized:** Fully containerized environment with `Docker` and `docker-compose`.

## 🛠 Technology Stack

*   **Core:** Java 17+, Spring Boot
*   **Build System:** Maven
*   **Database:** PostgreSQL
*   **Messaging:** RabbitMQ
*   **Containerization:** Docker, Docker Compose
*   **Testing:** JUnit 5, Mockito, Testcontainers

## 📂 Project Structure

This project follows a standard Maven directory layout. For a detailed file tree and status of files, please refer to [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md).

### Key Directories

*   `src/main/java`: Source code for the application.
    *   `config`: Configuration for App, Security, JPA, RabbitMQ, etc.
    *   `controller`: REST controllers for Prices and Arbitrage.
    *   `service`: Core business logic (Fetchers, Arbitrage Service, Price Engine).
    *   `domain`: Entity models (CurrencyPair, PriceTick, ArbitrageOpportunity).
*   `src/test/java`: Comprehensive unit and integration tests.
*   `implementatonDocs`: Documentation covering various development phases (Phase 1-10).
*   `resources/db/migration`: SQL scripts for database schema evolution.

## ⚡ Getting Started

### Prerequisites

*   Java 17 SDK
*   Docker & Docker Compose
*   Maven (Wrapper script `mvnw` is provided)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone <your-repo-url>
    cd CPA
    ```

2.  **Build the project:**
    ```bash
    ./mvnw clean install
    ```

### Running the Application

**Option 1: Using Docker Compose (Recommended)**

This starts the application along with the required PostgreSQL and RabbitMQ containers.

```bash
docker-compose up --build
```

**Option 2: Running Locally**

Ensure you have PostgreSQL and RabbitMQ running locally, or configure `application.properties` to point to your instances.

```bash
./mvnw spring-boot:run
```

## 🧪 Testing

The project maintains a high standard of code quality with extensive tests.

To run unit and integration tests:

```bash
./mvnw test
```

## 📖 Documentation

Detailed implementation logs and summaries for the project's development phases can be found in the `implementatonDocs/` directory.

---
*Generated based on project structure on December 15, 2025.*
