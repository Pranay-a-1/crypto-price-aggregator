# Crypto Price Aggregator (CPA)

![Java CI with Maven](https://github.com/Pranay-a-1/crypto-price-aggregator/actions/workflows/maven.yml/badge.svg?branch=springProject)

A robust Java Spring Boot application designed to aggregate real-time cryptocurrency prices from multiple exchanges and identify profitable arbitrage opportunities.

## 🚀 Features

*   **Multi-Exchange Integration:** Fetches real-time market data from major exchanges including **Binance**, **Coinbase**, and **Kraken**.
*   **Arbitrage Detection:** Continuously analyzes price disparities across exchanges to detect and persist arbitrage opportunities.
*   **Premium Web Frontend:** Modern, responsive dashboard with real-time price monitoring, exchange comparison, and arbitrage visualization.
*   **External Access:** Integrated **ngrok** tunneling for instant public URL generation - share your local app with anyone, anywhere.
*   **High Concurrency:** Utilizes concurrent fetching strategies and `ManualConcurrentPriceEngine` for optimized performance.
*   **Event-Driven Architecture:** Leverages **RabbitMQ** for asynchronous price tick processing and system decoupling.
*   **Real-Time Data:** Supports WebSocket connections for low-latency updates (with rate limiting).
*   **Persistence:** Stores market data and arbitrage events in **PostgreSQL**, with schema management handled by **Flyway**.
*   **Resilience & Monitoring:** Includes custom health indicators, metrics configuration, and fault-tolerant fetching logic.
*   **Code Quality:** Integrated **SonarQube** for continuous code quality analysis, security scanning, and technical debt tracking.
*   **Dockerized:** Fully containerized environment with `Docker` and `docker-compose`.

## 🛠 Technology Stack

**Backend:**
*   **Core:** Java 17+, Spring Boot
*   **Build System:** Maven
*   **Database:** PostgreSQL
*   **Messaging:** RabbitMQ
*   **Containerization:** Docker, Docker Compose
*   **External Access:** Ngrok (integrated with Docker Compose)
*   **Code Quality:** SonarQube, JaCoCo (code coverage)
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
*   Ngrok account (optional, for external access via public URL)

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

**Option 1: Using Docker Compose with Ngrok (Recommended)**

This starts the application along with PostgreSQL, RabbitMQ, and an ngrok tunnel for external access.

1. **Set up your ngrok auth token:**
   
   Create a `.env` file in the project root (already exists):
   ```bash
   NGROK_AUTHTOKEN=your_ngrok_token_here
   ```
   
   Get your free ngrok token at [ngrok.com](https://ngrok.com/) after signing up.

2. **Start all services:**
   ```bash
   sudo docker compose up --build
   ```

3. **Access the application:**
   - **Local access:** `http://localhost:8080/frontend/index.html`
   - **Public ngrok URL:** Check the ngrok web interface at `http://localhost:4040` to get your public URL
   - **Ngrok API:** `curl http://localhost:4040/api/tunnels` to view tunnel details

The application will be accessible both locally and via a public ngrok URL, making it easy to share and test from anywhere.

**Option 2: Using Docker Compose without Ngrok**

If you don't need external access, you can start just the core services:

```bash
sudo docker compose up --build db rabbitmq app
```

**Option 3: Running Locally**

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

## 🚀 Deployment

The project supports deployment to **GitHub Pages** for the frontend with the backend hosted separately.

### Quick Deploy to GitHub Pages

1. Push your code to GitHub
2. Enable GitHub Pages in repository settings (Source: GitHub Actions)
3. The workflow will automatically deploy on push to `main`
4. Access at: `https://<username>.github.io/<repo-name>/`

### Backend Hosting Options

Since GitHub Pages only hosts static files, deploy the backend to:
- **Local PC with ngrok/localtunnel** (for testing)
- **Render/Railway/Fly.io** (free tiers available)
- **AWS/GCP/Azure** (for production)

For detailed deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md).

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

## 📐 Design Documentation

Comprehensive design documents for understanding the project architecture and preparing for technical interviews:

- **[DESIGN.md](docs/DESIGN.md)**: System architecture, component design, data models, and deployment
- **[TECHNICAL_DEEPDIVE.md](docs/TECHNICAL_DEEPDIVE.md)**: Implementation details, concurrency patterns, and code architecture
- **[DESIGN_DOCS_README.md](docs/DESIGN_DOCS_README.md)**: Overview of all design documents and how to use them
- **[SONARQUBE_WORKFLOW.md](docs/SONARQUBE_WORKFLOW.md)**: Guide for running code quality analysis with SonarQube

These documents cover architectural decisions, design patterns, scalability considerations.

## 🔍 Code Quality Analysis

This project uses **SonarQube** for continuous code quality monitoring and security analysis.

### Running SonarQube Analysis

1. **Start SonarQube:**
   ```bash
   sudo docker compose up -d sonarqube-db sonarqube
   ```

2. **Access Dashboard:**
   ```
   http://localhost:9000
   ```
   Default credentials: `admin` / `admin` (change on first login)

3. **Run Analysis:**
   ```bash
   ./mvnw clean verify sonar:sonar -Dsonar.token=YOUR_TOKEN
   ```

4. **View Results:**
   ```
   http://localhost:9000/dashboard?id=crypto-price-aggregator
   ```

For detailed instructions, see [SONARQUBE_WORKFLOW.md](docs/SONARQUBE_WORKFLOW.md).

**What's Analyzed:**
- Code quality metrics and maintainability
- Security vulnerabilities and hotspots
- Code coverage (via JaCoCo)
- Bugs and code smells
- Technical debt estimation

## 📖 Documentation

Detailed implementation logs and summaries for the project's development phases can be found in the `implementatonDocs/` directory.

---
*Generated based on project structure on December 15, 2025.*
