# Frontend - Crypto Price Aggregator

A premium vanilla HTML/CSS/JS frontend for real-time cryptocurrency price monitoring and arbitrage opportunity detection.

## 🎨 Features

- **Real-time Price Monitoring**: Aggregated best bid/ask prices across all exchanges
- **Exchange Comparison**: Individual price displays for Binance, Coinbase, and Kraken
- **Arbitrage Opportunities**: Table view showing profitable trading opportunities
- **Interactive Charts**: Visual price comparisons using Chart.js
- **Auto-Refresh**: Configurable automatic data updates (default: 5 seconds)
- **Premium Design**: Modern glassmorphism UI with vibrant gradients and smooth animations
- **Fully Responsive**: Mobile-first design that works on all devices

## 🛠 Tech Stack

- **HTML5**: Semantic markup with SEO optimization
- **CSS3**: Modern design system with CSS custom properties
  - Glassmorphism effects with backdrop blur
  - Gradient backgrounds and accents
  - Smooth animations and transitions
  - Responsive grid layouts
- **JavaScript (ES6+)**: Vanilla JavaScript with modular architecture
- **Chart.js**: Data visualization library
- **Google Fonts**: Inter font family

## 📂 File Structure

```
frontend/
├── index.html      # Main HTML structure
├── styles.css      # Complete design system and styles
├── app.js          # Application logic and API integration
└── README.md       # This file
```

## 🚀 Getting Started

### Prerequisites

The backend Spring Boot application must be running on `http://localhost:8080`

### Running the Frontend

**Option 1: Via Spring Boot (Recommended)**

The frontend is automatically served by Spring Boot:

```bash
# From project root
docker-compose up --build

# Access the frontend
open http://localhost:8080/frontend/index.html
```

**Option 2: Local Development Server**

For rapid frontend development, use a simple HTTP server:

```bash
cd frontend

# Using Python
python3 -m http.server 8000

# Using Node.js (if http-server is installed)
npx http-server -p 8000

# Access at http://localhost:8000
```

**Note**: If using a separate development server, update `API_BASE_URL` in `app.js` to point to your backend.

## 🔧 Configuration

Edit the configuration object in `app.js`:

```javascript
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api',  // Backend API URL
    AUTO_REFRESH_INTERVAL: 5000,                 // Auto-refresh interval (ms)
    CHART_UPDATE_INTERVAL: 10000,                // Chart update interval (ms)
    MAX_ARBITRAGE_LIMIT: 10,                     // Max arbitrage results
};
```

## 📡 API Integration

The frontend consumes the following REST endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/prices/{base}/{quote}` | GET | Aggregated best bid/ask |
| `/api/prices/{base}/{quote}/exchanges` | GET | Individual exchange prices |
| `/api/arbitrage/{base}/{quote}?limit={n}` | GET | Arbitrage opportunities |

### Example API Response Formats

**Aggregated Price:**
```json
{
  "bestBid": 50000.00,
  "bestBidExchange": "BINANCE",
  "bestAsk": 50050.00,
  "bestAskExchange": "COINBASE"
}
```

**Exchange Prices:**
```json
{
  "BINANCE": {
    "bid": 50000.00,
    "ask": 50050.00,
    "lastPrice": 50025.00,
    "volume24h": 1250000
  },
  "COINBASE": { ... },
  "KRAKEN": { ... }
}
```

**Arbitrage Opportunities:**
```json
[
  {
    "buyExchange": "BINANCE",
    "buyPrice": 50000.00,
    "sellExchange": "COINBASE",
    "sellPrice": 50100.00,
    "profitPercentage": 0.2,
    "timestamp": "2025-12-15T00:50:00Z"
  }
]
```

## 🎯 Features in Detail

### 1. Currency Pair Selection
- Dropdown selector for major trading pairs (BTC/USD, ETH/USD, etc.)
- Instant data refresh on pair change

### 2. Auto-Refresh
- Toggle switch to enable/disable automatic updates
- Configurable refresh interval
- Visual indicators for loading states

### 3. Price Dashboard
- **Aggregated Card**: Best bid/ask across all exchanges with spread calculation
- **Exchange Cards**: Individual cards for each exchange showing bid, ask, last price, and volume
- **Price Animations**: Visual feedback when prices change (green for up, red for down)

### 4. Arbitrage Table
- Sortable table showing buy/sell opportunities
- Profit percentage calculations
- Timestamp of each opportunity
- Empty state message when no opportunities exist

### 5. Price Chart
- Bar chart comparing bid/ask prices across exchanges
- Responsive and interactive
- Updates automatically with data refresh

## 🎨 Design System

### Color Palette
- **Background**: Deep purple/blue gradient (`#1e1b4b` → `#1a1333` → `#0f172a`)
- **Accent Gradients**: Purple to Cyan (`#8b5cf6` → `#06b6d4`)
- **Success**: Emerald green (`#10b981`)
- **Danger**: Red (`#ef4444`)

### Typography
- **Font**: Inter (Google Fonts)
- **Scale**: 12px - 32px with consistent hierarchy

### Components
- **Glass Cards**: Glassmorphism with backdrop blur
- **Animations**: Smooth transitions and micro-interactions
- **Responsive Grid**: Mobile-first responsive layouts

## 🐛 Troubleshooting

### Backend Connection Issues
- Ensure backend is running: `docker-compose up`
- Check backend health: `curl http://localhost:8080/actuator/health`
- Verify CORS settings in `SecurityConfig.java`

### No Data Displaying
- Check browser console (F12) for errors
- Verify API endpoints return data: `curl http://localhost:8080/api/prices/BTC/USD`
- Ensure exchanges are returning mock/real data

### Chart Not Rendering
- Verify Chart.js CDN is loading (check Network tab)
- Check console for Chart.js errors
- Ensure canvas element exists in DOM

## 🚀 Future Enhancements

- [ ] WebSocket integration for true real-time updates
- [ ] Price alerts and notifications
- [ ] Historical price charts with time series data
- [ ] User preferences and settings persistence
- [ ] Dark/light theme toggle
- [ ] Export arbitrage opportunities to CSV
- [ ] Advanced filtering and search
- [ ] Mobile app (PWA)

## 📄 License

Part of the Crypto Price Aggregator project.

---

**Developed with ❤️ using vanilla HTML/CSS/JS**
