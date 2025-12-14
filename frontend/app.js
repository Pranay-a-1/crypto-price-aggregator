// ===================================
// Application Configuration
// ===================================
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api',
    AUTO_REFRESH_INTERVAL: 5000, // 5 seconds
    CHART_UPDATE_INTERVAL: 10000, // 10 seconds
    MAX_ARBITRAGE_LIMIT: 10,
    // Basic Auth credentials (default from Spring Security)
    AUTH_USERNAME: 'user',
    AUTH_PASSWORD: 'password',
};

// ===================================
// Application State
// ===================================
const state = {
    currentPair: { base: 'BTC', quote: 'USD' },
    autoRefresh: true,
    lastUpdate: null,
    priceHistory: [],
    refreshInterval: null,
};

// ===================================
// DOM Elements
// ===================================
const elements = {
    currencyPair: document.getElementById('currencyPair'),
    autoRefresh: document.getElementById('autoRefresh'),
    refreshBtn: document.getElementById('refreshBtn'),
    lastUpdated: document.getElementById('lastUpdated'),
    connectionStatus: document.getElementById('connectionStatus'),

    // Aggregated Price Elements
    bestBid: document.getElementById('bestBid'),
    bestBidExchange: document.getElementById('bestBidExchange'),
    bestAsk: document.getElementById('bestAsk'),
    bestAskExchange: document.getElementById('bestAskExchange'),
    spread: document.getElementById('spread'),
    spreadPercent: document.getElementById('spreadPercent'),

    // Exchange Grid
    exchangeGrid: document.getElementById('exchangeGrid'),

    // Arbitrage Table
    arbitrageTableBody: document.getElementById('arbitrageTableBody'),
    noArbitrageMessage: document.getElementById('noArbitrageMessage'),

    // Chart
    priceChart: document.getElementById('priceChart'),
};

// ===================================
// API Service
// ===================================
const api = {
    /**
     * Get authentication headers for API requests
     */
    getAuthHeaders() {
        const credentials = btoa(`${CONFIG.AUTH_USERNAME}:${CONFIG.AUTH_PASSWORD}`);
        return {
            'Authorization': `Basic ${credentials}`,
            'Content-Type': 'application/json'
        };
    },

    /**
     * Fetch aggregated price for a currency pair
     */
    async getAggregatedPrice(base, quote) {
        const url = `${CONFIG.API_BASE_URL}/prices/${base}/${quote}`;
        const response = await fetch(url, {
            headers: this.getAuthHeaders()
        });

        if (response.status === 404) {
            return null; // No price available
        }

        if (!response.ok) {
            throw new Error(`Failed to fetch aggregated price: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * Fetch individual exchange prices for a currency pair
     */
    async getExchangePrices(base, quote) {
        const url = `${CONFIG.API_BASE_URL}/prices/${base}/${quote}/exchanges`;
        const response = await fetch(url, {
            headers: this.getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch exchange prices: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * Fetch arbitrage opportunities
     */
    async getArbitrageOpportunities(base, quote, limit = CONFIG.MAX_ARBITRAGE_LIMIT) {
        const url = `${CONFIG.API_BASE_URL}/arbitrage/${base}/${quote}?limit=${limit}`;
        const response = await fetch(url, {
            headers: this.getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch arbitrage opportunities: ${response.statusText}`);
        }

        return await response.json();
    },
};

// ===================================
// UI Update Functions
// ===================================
const ui = {
    /**
     * Update aggregated price card
     */
    updateAggregatedPrice(data) {
        if (!data || data.bestBid === undefined || data.bestBid === null || data.bestAsk === undefined || data.bestAsk === null) {
            elements.bestBid.textContent = 'N/A';
            elements.bestBidExchange.textContent = 'No data available';
            elements.bestAsk.textContent = 'N/A';
            elements.bestAskExchange.textContent = 'No data available';
            elements.spread.textContent = 'N/A';
            elements.spreadPercent.textContent = '';
            return;
        }

        // Update best bid
        const prevBid = parseFloat(elements.bestBid.textContent.replace('$', '').replace(/,/g, '')) || 0;
        elements.bestBid.textContent = `$${data.bestBid.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        elements.bestBidExchange.textContent = data.bestBidExchange || 'Unknown';

        // Animate if price changed
        if (prevBid > 0) {
            if (data.bestBid > prevBid) {
                elements.bestBid.classList.add('price-up');
                setTimeout(() => elements.bestBid.classList.remove('price-up'), 500);
            } else if (data.bestBid < prevBid) {
                elements.bestBid.classList.add('price-down');
                setTimeout(() => elements.bestBid.classList.remove('price-down'), 500);
            }
        }

        // Update best ask
        const prevAsk = parseFloat(elements.bestAsk.textContent.replace('$', '').replace(/,/g, '')) || 0;
        elements.bestAsk.textContent = `$${data.bestAsk.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        elements.bestAskExchange.textContent = data.bestAskExchange || 'Unknown';

        // Animate if price changed
        if (prevAsk > 0) {
            if (data.bestAsk > prevAsk) {
                elements.bestAsk.classList.add('price-up');
                setTimeout(() => elements.bestAsk.classList.remove('price-up'), 500);
            } else if (data.bestAsk < prevAsk) {
                elements.bestAsk.classList.add('price-down');
                setTimeout(() => elements.bestAsk.classList.remove('price-down'), 500);
            }
        }

        // Calculate and display spread
        const spread = data.bestAsk - data.bestBid;
        const spreadPercent = ((spread / data.bestBid) * 100).toFixed(3);

        elements.spread.textContent = `$${spread.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        elements.spreadPercent.textContent = `${spreadPercent}%`;

        // Remove loading state
        elements.bestBid.classList.remove('loading');
        elements.bestAsk.classList.remove('loading');
        elements.spread.classList.remove('loading');
    },

    /**
     * Update exchange price cards
     */
    updateExchangeCards(exchangePrices) {
        const exchanges = Object.keys(exchangePrices);

        if (exchanges.length === 0) {
            elements.exchangeGrid.innerHTML = '<p class="no-data-message">No exchange data available</p>';
            return;
        }

        // Clear grid
        elements.exchangeGrid.innerHTML = '';

        // Create card for each exchange
        exchanges.forEach(exchangeName => {
            const price = exchangePrices[exchangeName];

            // Skip if price data is invalid
            if (!price || typeof price !== 'object') {
                return;
            }

            const card = document.createElement('div');
            card.className = 'exchange-card glass-card';

            const bid = (price.bid !== undefined && price.bid !== null) ? price.bid.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
            const ask = (price.ask !== undefined && price.ask !== null) ? price.ask.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
            const lastPrice = (price.lastPrice !== undefined && price.lastPrice !== null) ? price.lastPrice.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
            const volume = (price.volume24h !== undefined && price.volume24h !== null) ? price.volume24h.toLocaleString('en-US', { maximumFractionDigits: 0 }) : 'N/A';

            card.innerHTML = `
                <div class="exchange-header">
                    <h3 class="exchange-name">${exchangeName}</h3>
                    <div class="exchange-status"></div>
                </div>
                <div class="exchange-prices">
                    <div class="exchange-price-row">
                        <span class="exchange-price-label">Bid</span>
                        <span class="exchange-price-value">${bid !== 'N/A' ? '$' + bid : bid}</span>
                    </div>
                    <div class="exchange-price-row">
                        <span class="exchange-price-label">Ask</span>
                        <span class="exchange-price-value">${ask !== 'N/A' ? '$' + ask : ask}</span>
                    </div>
                    <div class="exchange-price-row">
                        <span class="exchange-price-label">Last</span>
                        <span class="exchange-price-value">${lastPrice !== 'N/A' ? '$' + lastPrice : lastPrice}</span>
                    </div>
                    <div class="exchange-price-row">
                        <span class="exchange-price-label">Volume</span>
                        <span class="exchange-price-value">${volume}</span>
                    </div>
                </div>
            `;

            elements.exchangeGrid.appendChild(card);
        });
    },

    /**
     * Update arbitrage opportunities table
     */
    updateArbitrageTable(opportunities) {
        if (!opportunities || opportunities.length === 0) {
            elements.arbitrageTableBody.innerHTML = '';
            elements.noArbitrageMessage.style.display = 'flex';
            return;
        }

        elements.noArbitrageMessage.style.display = 'none';

        const rows = opportunities.map(opp => {
            // Skip invalid opportunities
            if (!opp || typeof opp !== 'object') {
                return '';
            }

            const profitPercent = (opp.profitPercentage !== undefined && opp.profitPercentage !== null) ? opp.profitPercentage.toFixed(2) : '0.00';
            const timestamp = opp.timestamp ? new Date(opp.timestamp).toLocaleString('en-US', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            }) : 'N/A';

            const buyPrice = (opp.buyPrice !== undefined && opp.buyPrice !== null) ? opp.buyPrice.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
            const sellPrice = (opp.sellPrice !== undefined && opp.sellPrice !== null) ? opp.sellPrice.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';

            return `
                <tr>
                    <td>${opp.buyExchange || 'Unknown'}</td>
                    <td>${buyPrice !== 'N/A' ? '$' + buyPrice : buyPrice}</td>
                    <td>${opp.sellExchange || 'Unknown'}</td>
                    <td>${sellPrice !== 'N/A' ? '$' + sellPrice : sellPrice}</td>
                    <td class="profit-positive">+${profitPercent}%</td>
                    <td>${timestamp}</td>
                </tr>
            `;
        }).join('');

        elements.arbitrageTableBody.innerHTML = rows;
    },

    /**
     * Update last updated timestamp
     */
    updateLastUpdated() {
        const now = new Date();
        state.lastUpdate = now;
        elements.lastUpdated.textContent = now.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    },

    /**
     * Show error message
     */
    showError(message) {
        console.error('Error:', message);
        elements.connectionStatus.textContent = 'Error';
        elements.connectionStatus.className = 'status-value';
        elements.connectionStatus.style.color = 'var(--color-danger)';

        // Show user-friendly error in console
        alert(`Error: ${message}\n\nPlease ensure the backend server is running at ${CONFIG.API_BASE_URL}`);
    },

    /**
     * Update connection status
     */
    updateConnectionStatus(connected) {
        if (connected) {
            elements.connectionStatus.textContent = 'Connected';
            elements.connectionStatus.className = 'status-value status-connected';
            elements.connectionStatus.style.color = 'var(--color-success)';
        } else {
            elements.connectionStatus.textContent = 'Disconnected';
            elements.connectionStatus.className = 'status-value';
            elements.connectionStatus.style.color = 'var(--color-danger)';
        }
    },
};

// ===================================
// Chart Management
// ===================================
let priceChart = null;

const chart = {
    /**
     * Initialize the price chart
     */
    init() {
        const ctx = elements.priceChart.getContext('2d');

        priceChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Bid Price',
                    data: [],
                    backgroundColor: 'rgba(139, 92, 246, 0.6)',
                    borderColor: 'rgba(139, 92, 246, 1)',
                    borderWidth: 2,
                }, {
                    label: 'Ask Price',
                    data: [],
                    backgroundColor: 'rgba(6, 182, 212, 0.6)',
                    borderColor: 'rgba(6, 182, 212, 1)',
                    borderWidth: 2,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: '#f8fafc',
                            font: {
                                size: 12,
                                family: 'Inter'
                            }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(15, 10, 31, 0.9)',
                        titleColor: '#f8fafc',
                        bodyColor: '#cbd5e1',
                        borderColor: 'rgba(255, 255, 255, 0.1)',
                        borderWidth: 1,
                        padding: 12,
                        displayColors: true,
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            color: '#cbd5e1',
                            callback: function (value) {
                                return '$' + value.toLocaleString();
                            }
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.05)'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#cbd5e1'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.05)'
                        }
                    }
                }
            }
        });
    },

    /**
     * Update chart with exchange prices
     */
    update(exchangePrices) {
        if (!priceChart || !exchangePrices || typeof exchangePrices !== 'object') return;

        const exchanges = Object.keys(exchangePrices);
        const bidPrices = exchanges.map(ex => {
            const price = exchangePrices[ex];
            return (price && price.bid !== undefined && price.bid !== null) ? price.bid : 0;
        });
        const askPrices = exchanges.map(ex => {
            const price = exchangePrices[ex];
            return (price && price.ask !== undefined && price.ask !== null) ? price.ask : 0;
        });

        priceChart.data.labels = exchanges;
        priceChart.data.datasets[0].data = bidPrices;
        priceChart.data.datasets[1].data = askPrices;

        priceChart.update();
    }
};

// ===================================
// Data Fetching
// ===================================
async function fetchAllData() {
    const { base, quote } = state.currentPair;

    try {
        // Fetch all data in parallel
        const [aggregatedPrice, exchangePrices, arbitrageOpportunities] = await Promise.all([
            api.getAggregatedPrice(base, quote),
            api.getExchangePrices(base, quote),
            api.getArbitrageOpportunities(base, quote)
        ]);

        // Update UI
        ui.updateAggregatedPrice(aggregatedPrice);
        ui.updateExchangeCards(exchangePrices);
        ui.updateArbitrageTable(arbitrageOpportunities);
        chart.update(exchangePrices);
        ui.updateLastUpdated();
        ui.updateConnectionStatus(true);

    } catch (error) {
        ui.showError(error.message);
        ui.updateConnectionStatus(false);
    }
}

// ===================================
// Event Handlers
// ===================================
function handleCurrencyPairChange() {
    const [base, quote] = elements.currencyPair.value.split('/');
    state.currentPair = { base, quote };
    fetchAllData();
}

function handleAutoRefreshToggle() {
    state.autoRefresh = elements.autoRefresh.checked;

    if (state.autoRefresh) {
        startAutoRefresh();
    } else {
        stopAutoRefresh();
    }
}

function handleManualRefresh() {
    fetchAllData();

    // Add visual feedback
    elements.refreshBtn.style.transform = 'rotate(360deg)';
    setTimeout(() => {
        elements.refreshBtn.style.transform = 'rotate(0deg)';
    }, 500);
}

function startAutoRefresh() {
    if (state.refreshInterval) {
        clearInterval(state.refreshInterval);
    }

    state.refreshInterval = setInterval(() => {
        if (state.autoRefresh) {
            fetchAllData();
        }
    }, CONFIG.AUTO_REFRESH_INTERVAL);
}

function stopAutoRefresh() {
    if (state.refreshInterval) {
        clearInterval(state.refreshInterval);
        state.refreshInterval = null;
    }
}

// ===================================
// Initialization
// ===================================
function init() {
    // Set up event listeners
    elements.currencyPair.addEventListener('change', handleCurrencyPairChange);
    elements.autoRefresh.addEventListener('change', handleAutoRefreshToggle);
    elements.refreshBtn.addEventListener('click', handleManualRefresh);

    // Initialize chart
    chart.init();

    // Initial data fetch
    fetchAllData();

    // Start auto-refresh if enabled
    if (state.autoRefresh) {
        startAutoRefresh();
    }

    console.log('🚀 Crypto Price Aggregator initialized');
    console.log('📊 Fetching data from:', CONFIG.API_BASE_URL);
}

// Start the application when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}
