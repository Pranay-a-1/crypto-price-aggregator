# Crypto Price Aggregator (CPA)

![Java CI with Maven](https://github.com/Pranay-a-1/crypto-price-aggregator/actions/workflows/maven.yml/badge.svg)

A robust Java Spring Boot application designed to aggregate real-time cryptocurrency prices from multiple exchanges and identify profitable arbitrage opportunities.

## 🚀 Features

*   **Multi-Exchange Integration:** Fetches real-time market data from major exchanges including **Binance**, **Coinbase**, and **Kraken**.
*   **Arbitrage Detection:** Continuously analyzes price disparities across exchanges to detect and persist arbitrage opportunities.
*   **Premium Web Frontend:** Modern, responsive dashboard with real-time price monitoring, exchange comparison, and arbitrage visualization.
*   **High Concurrency:** Utilizes concurrent fetching strategies and `ManualConcurrentPriceEngine` for optimized performance.
*   **Event-Driven Architecture:** Leverages **RabbitMQ** for asynchronous price tick processing and system decoupling.
*   **Real-Time Data:** Supports WebSocket connections for low-latency updates (with rate limiting).
*   **Persistence:** Stores market data and arbitrage events in **PostgreSQL**, with schema management handled by **Flyway**.
*   **Resilience & Monitoring:** Includes custom health indicators, metrics configuration, and fault-tolerant fetching logic.
*   **Dockerized:** Fully containerized environment with `Docker` and `docker-compose`.

## 🛠 Technology Stack

**Backend:**
*   **Core:** Java 17+, Spring Boot
*   **Build System:** Maven
*   **Database:** PostgreSQL
*   **Messaging:** RabbitMQ
*   **Containerization:** Docker, Docker Compose
*   **Testing:** JUnit 5, Mockito, Testcontainers

**Frontend:**
*   **Core:** HTML5, CSS3, JavaScript (ES6+)
*   **Visualization:** Chart.js
*   **Design:** Vanilla CSS with glassmorphism and modern gradients

## 📂 Project Structure

This project follows a standard Maven directory layout. For a detailed file tree and status of files, please refer to [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md).

### Key Directories

*   `frontend`: Premium web interface for real-time price monitoring and arbitrage viewing.
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
sudo docker compose up --build
```

**Option 2: Running Locally**

Ensure you have PostgreSQL and RabbitMQ running locally, or configure `application.properties` to point to your instances.

```bash
./mvnw spring-boot:run
```

### Accessing the Frontend

Once the application is running, access the web interface at:

```
http://localhost:8080/frontend/index.html
```

**Default Credentials:**
- Username: `user`
- Password: `password`

The frontend provides a premium dashboard with:
- Real-time price monitoring across exchanges
- Arbitrage opportunity detection
- Interactive price comparison charts
- Auto-refresh capabilities

## 🧪 Testing

The project maintains a high standard of code quality with extensive tests.

To run unit and integration tests:

```bash
./mvnw test
```

## 🎨 Frontend

The application includes a premium web interface built with vanilla HTML/CSS/JavaScript.

**Features:**
- Modern glassmorphism design with vibrant gradients
- Real-time price updates (5-second auto-refresh)
- Exchange-by-exchange price comparison
- Arbitrage opportunities table
- Interactive Chart.js visualizations
- Fully responsive mobile-first design

For detailed frontend documentation, see [frontend/README.md](frontend/README.md).

## 📖 Documentation

Detailed implementation logs and summaries for the project's development phases can be found in the `implementatonDocs/` directory.

---
*Generated based on project structure on December 15, 2025.*
