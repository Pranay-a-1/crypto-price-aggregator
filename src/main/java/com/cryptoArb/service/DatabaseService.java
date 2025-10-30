package com.cryptoArb.service;

import com.cryptoArb.domain.ArbitrageOpportunity;
import com.cryptoArb.domain.PriceTick;

import java.sql.*;

/**
 * Manages all database persistence logic using Core JDBC (java.sql.*).
 */
public class DatabaseService {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    // The SQL query is defined as a constant.
    // We use "?" as placeholders for our PreparedStatement.
    private static final String INSERT_PRICE_TICK_SQL = """
        INSERT INTO price_tick
        (base_currency, quote_currency, exchange, timestamp, bid_price, ask_price)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String INSERT_ARBITRAGE_OPPORTUNITY_SQL = """
        INSERT INTO arbitrage_opportunity
        (base_currency, quote_currency, timestamp, buy_exchange, buy_price, sell_exchange, sell_price, profit_percentage)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;



    /**
     * Constructs a new DatabaseService with connection details.
     *
     * @param jdbcUrl  The JDBC connection string (e.g., "jdbc:postgresql://...")
     * @param username The database username
     * @param password The database password
     */
    public DatabaseService(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Saves a single PriceTick to the database.
     *
     * @param tick The PriceTick object to save.
     */
    public void saveTick(PriceTick tick) {
        // We use try-with-resources to automatically manage the
        // lifecycle of our Connection and PreparedStatement.
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement stmt = conn.prepareStatement(INSERT_PRICE_TICK_SQL)) {

            // Set the parameters for the PreparedStatement
            // This is safer than string concatenation (prevents SQL injection)

            // 1. base_currency (from CurrencyPair record)
            stmt.setString(1, tick.pair().base());
            // 2. quote_currency (from CurrencyPair record)
            stmt.setString(2, tick.pair().quote());
            // 3. exchange (from Exchange record)
            stmt.setString(3, tick.exchange().id());
            // 4. timestamp (needs conversion from Instant to sql.Timestamp)
            stmt.setTimestamp(4, Timestamp.from(tick.timestamp()));
            // 5. bid_price (BigDecimal)
            stmt.setBigDecimal(5, tick.bidPrice());
            // 6. ask_price (BigDecimal)
            stmt.setBigDecimal(6, tick.askPrice());

            // Execute the insert statement
            stmt.executeUpdate();

        } catch (SQLException e) {
            // In a real application, we would have a more robust
            // exception handling strategy (e.g., custom exceptions, logging)
            // For now, we just print the error.
            System.err.println("Error saving PriceTick: " + e.getMessage());
            // We can also re-throw it as a RuntimeException if we
            // consider this a fatal operation for our service.
            throw new RuntimeException("Failed to save tick", e);
        }
    }



    /**
     * Saves a single ArbitrageOpportunity to the database.
     *
     * @param opportunity The ArbitrageOpportunity object to save.
     */
    public void saveOpportunity(ArbitrageOpportunity opportunity) {
        // Use try-with-resources again for our Connection and PreparedStatement
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement stmt = conn.prepareStatement(INSERT_ARBITRAGE_OPPORTUNITY_SQL)) {

            // Set the parameters for the PreparedStatement
            // 1. base_currency
            stmt.setString(1, opportunity.pair().base());
            // 2. quote_currency
            stmt.setString(2, opportunity.pair().quote());
            // 3. timestamp
            stmt.setTimestamp(3, Timestamp.from(opportunity.timestamp()));
            // 4. buy_exchange
            stmt.setString(4, opportunity.buyExchange().id());
            // 5. buy_price
            stmt.setBigDecimal(5, opportunity.buyPrice());
            // 6. sell_exchange
            stmt.setString(6, opportunity.sellExchange().id());
            // 7. sell_price
            stmt.setBigDecimal(7, opportunity.sellPrice());
            // 8. profit_percentage
            stmt.setBigDecimal(8, opportunity.profitPercentage());

            // Execute the insert statement
            stmt.executeUpdate();

        } catch (SQLException e) {
            // Re-throw as a runtime exception
            System.err.println("Error saving ArbitrageOpportunity: " + e.getMessage());
            throw new RuntimeException("Failed to save opportunity", e);
        }
    }
}
