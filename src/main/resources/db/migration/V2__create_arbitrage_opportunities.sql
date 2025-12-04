-- V2: Arbitrage Opportunities Table
-- This migration adds the new arbitrage_opportunities table for Phase 10
-- Stores detected arbitrage opportunities across exchanges

CREATE TABLE arbitrage_opportunities (
    id BIGSERIAL PRIMARY KEY,
    base_currency VARCHAR(255) NOT NULL,
    quote_currency VARCHAR(255) NOT NULL,
    buy_exchange VARCHAR(255) NOT NULL,
    sell_exchange VARCHAR(255) NOT NULL,
    buy_price NUMERIC(19,8) NOT NULL,
    sell_price NUMERIC(19,8) NOT NULL,
    profit_percentage NUMERIC(10,4) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    
    -- Business rule constraints
    CONSTRAINT chk_arb_buy_price_positive CHECK (buy_price > 0),
    CONSTRAINT chk_arb_sell_price_positive CHECK (sell_price > 0),
    CONSTRAINT chk_arb_profit_positive CHECK (profit_percentage > 0),
    CONSTRAINT chk_arb_buy_less_than_sell CHECK (buy_price < sell_price)
);

-- Index for queries by currency pair (most common access pattern)
CREATE INDEX idx_arb_currency_pair 
    ON arbitrage_opportunities(base_currency, quote_currency);

-- Index for time-based queries (recent opportunities)
CREATE INDEX idx_arb_detected_at 
    ON arbitrage_opportunities(detected_at DESC);

-- Composite index for currency pair + time queries
CREATE INDEX idx_arb_pair_time 
    ON arbitrage_opportunities(base_currency, quote_currency, detected_at DESC);

-- Comment for documentation
COMMENT ON TABLE arbitrage_opportunities IS 
    'Stores detected arbitrage opportunities where an asset can be bought on one exchange and sold on another for profit';

COMMENT ON COLUMN arbitrage_opportunities.profit_percentage IS 
    'Profit percentage calculated as ((sell_price - buy_price) / buy_price) * 100';
