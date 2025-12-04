-- V1: Initial Schema - Baseline for existing price_ticks table
-- This migration captures the existing database schema created by Hibernate in previous phases
-- Following Flyway naming convention: V{version}__{description}.sql

CREATE TABLE IF NOT EXISTS price_ticks (
    id BIGSERIAL PRIMARY KEY,
    base VARCHAR(255) NOT NULL,
    quote VARCHAR(255) NOT NULL,
    exchange VARCHAR(255) NOT NULL,
    bid NUMERIC(19,8) NOT NULL,
    ask NUMERIC(19,8) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT chk_bid_non_negative CHECK (bid >= 0),
    CONSTRAINT chk_ask_non_negative CHECK (ask >= 0),
    CONSTRAINT chk_bid_le_ask CHECK (bid <= ask)
);

-- Index for common queries by currency pair and timestamp
CREATE INDEX IF NOT EXISTS idx_price_ticks_pair_timestamp 
    ON price_ticks(base, quote, timestamp DESC);

-- Index for exchange-specific queries
CREATE INDEX IF NOT EXISTS idx_price_ticks_exchange 
    ON price_ticks(exchange);
