package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;
import com.cryptoArb.domain.CurrencyPair;
import com.cryptoArb.domain.Exchange;
import com.cryptoArb.domain.PriceTick;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;


// 1. Enable Testcontainers for this test class
// Testcontainers will manage the lifecycle of our containers
// (e.g., start before tests, stop after tests)
// Testcontainers annotation means docker must be running for these tests to work
@Testcontainers
@DisplayName("DatabaseService Integration Tests")
class DatabaseServiceTest {

    // 2. Define the PostgreSQL container
    // It will use a standard 'postgres' image.
    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15");

    private DatabaseService databaseService;
    private Connection testConnection; // For raw JDBC validation

    @BeforeEach
    void setUp() throws SQLException {
        // 3. Get the dynamic connection details from the running container
        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();

        // 4. Create the service instance (This line will fail to compile!)
        databaseService = new DatabaseService(jdbcUrl, username, password);

        // 5. Create our *own* connection for verifying the test results
        testConnection = DriverManager.getConnection(jdbcUrl, username, password);

        // 6. Manually create our table schema for this test run
        // In a "real" app, a tool like Flyway or Liquibase would do this.
        // For our test, we do it manually.
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS price_tick (
                id SERIAL PRIMARY KEY,
                base_currency VARCHAR(10) NOT NULL,
                quote_currency VARCHAR(10) NOT NULL,
                exchange VARCHAR(50) NOT NULL,
                timestamp TIMESTAMPTZ NOT NULL,
                bid_price DECIMAL(20, 8) NOT NULL,
                ask_price DECIMAL(20, 8) NOT NULL
            );
            """;

        String createArbTableSql = """
            CREATE TABLE IF NOT EXISTS arbitrage_opportunity (
                id SERIAL PRIMARY KEY,
                base_currency VARCHAR(10) NOT NULL,
                quote_currency VARCHAR(10) NOT NULL,
                timestamp TIMESTAMPTZ NOT NULL,
                buy_exchange VARCHAR(50) NOT NULL,
                buy_price DECIMAL(20, 8) NOT NULL,
                sell_exchange VARCHAR(50) NOT NULL,
                sell_price DECIMAL(20, 8) NOT NULL,
                profit_percentage DECIMAL(10, 5) NOT NULL
            );
            """;

        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(createTableSql); // This one is from before
            stmt.execute(createArbTableSql); // This is the new one
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up resources after each test
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @Test
    @DisplayName("Should save a PriceTick to the database")
    void shouldSavePriceTickAndAllowRetrieval() throws SQLException { // void givenDbService_whenSaveTick_thenDataIsPersisted() {
        // --- Given ---
        // A sample PriceTick object
        PriceTick tick = new PriceTick(
                new CurrencyPair("BTC", "USD"),
                new Exchange("coinbase"),
                Instant.parse("2025-10-30T12:00:00Z"),
                new BigDecimal("60000.50"),
                new BigDecimal("60001.75")
        );

        // --- When ---
        // We call our (non-existent) save method
        // (This line will also fail to compile!)
        databaseService.saveTick(tick);

        // --- Then ---
        // We use our *test-only* connection to verify the data was saved correctly
        String verifySql = "SELECT * FROM price_tick WHERE exchange = 'coinbase'";
        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery(verifySql)) {

            assertTrue(rs.next(), "No data was found in the price_tick table");

            // Verify all columns were saved correctly
            assertEquals("BTC", rs.getString("base_currency"));
            assertEquals("USD", rs.getString("quote_currency"));
            assertEquals("coinbase", rs.getString("exchange"));
            // Use compareTo to check for numerical equality, ignoring scale
            // We expect the comparison result to be 0 (meaning "they are equal")
            assertEquals(0,
                    new BigDecimal("60000.50").compareTo(rs.getBigDecimal("bid_price")),
                    "Bid price numerical value mismatch");

            assertEquals(0,
                    new BigDecimal("60001.75").compareTo(rs.getBigDecimal("ask_price")),
                    "Ask price numerical value mismatch");

            // Timestamps are tricky. We verify it's the correct time.
            // .getTimestamp() returns a JDBC Timestamp, which we convert to Instant
            Instant dbTimestamp = rs.getTimestamp("timestamp").toInstant();
            assertEquals(tick.timestamp(), dbTimestamp);

            assertFalse(rs.next(), "More than one record was found");
        }
    }


    @Test
    @DisplayName("Should save an ArbitrageOpportunity to the database")
    void shouldSaveArbitrageOpportunityAndAllowRetrieval() throws SQLException {
        // --- Given ---
        // A sample ArbitrageOpportunity object
        ArbitrageOpportunity opportunity = new ArbitrageOpportunity(
                new CurrencyPair("ETH", "USD"),
                Instant.parse("2025-11-01T10:00:00Z"),
                new Exchange("kraken"),
                new BigDecimal("4000.10"),
                new Exchange("binance"),
                new BigDecimal("4005.15")
        );

        // --- When ---
        // We call our (non-existent) save method
        // (This line will fail to compile!)
        databaseService.saveOpportunity(opportunity);

        // --- Then ---
        // We use our test-only connection to verify
        String verifySql = "SELECT * FROM arbitrage_opportunity WHERE buy_exchange = 'kraken'";
        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery(verifySql)) {

            assertTrue(rs.next(), "No data was found in the arbitrage_opportunity table");

            // Verify all columns
            assertEquals("ETH", rs.getString("base_currency"));
            assertEquals("USD", rs.getString("quote_currency"));
            assertEquals("kraken", rs.getString("buy_exchange"));
            assertEquals("binance", rs.getString("sell_exchange"));

            // Use compareTo for BigDecimal assertions, just as we learned!
            assertEquals(0,
                    new BigDecimal("4000.10").compareTo(rs.getBigDecimal("buy_price")),
                    "Buy price numerical value mismatch");
            assertEquals(0,
                    new BigDecimal("4005.15").compareTo(rs.getBigDecimal("sell_price")),
                    "Sell price numerical value mismatch");
            assertEquals(0,
                    new BigDecimal("0.00126").compareTo(rs.getBigDecimal("profit_percentage")),
                    "Profit percentage numerical value mismatch");

            // Verify timestamp
            Instant dbTimestamp = rs.getTimestamp("timestamp").toInstant();
            assertEquals(opportunity.timestamp(), dbTimestamp);

            assertFalse(rs.next(), "More than one record was found");
        }
    }



}