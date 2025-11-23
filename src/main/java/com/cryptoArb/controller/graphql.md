# Testing GraphQL for REST Developers

If you are used to REST APIs, GraphQL might seem a bit different. Here is a quick guide to help you test our GraphQL endpoints.

## Key Differences

| Feature | REST | GraphQL |
|---------|------|---------|
| **Endpoint** | Multiple (e.g., `/api/prices`, `/api/arb`) | Single (usually `/graphql`) |
| **Method** | GET, POST, PUT, DELETE | Always **POST** |
| **Data Fetching** | Server defines what you get | **You** define what you want |
| **Status Codes** | 200, 404, 500, etc. | 200 (even for errors, check `errors` field) |

## How to Construct a Request

In REST, you might hit:
`GET /api/prices/BTC-USD`

In GraphQL, you send a **POST** request to `/graphql` with a JSON body containing a `query`.

### The JSON Body Structure
```json
{
  "query": "query { ... your graphql query ... }",
  "variables": { ... optional variables ... }
}
```

## Testing with cURL

Assuming the application is running on `localhost:8080`.

### 1. Get Consolidated Price
Fetch the price for `BTC-USD`. Note how we explicitly ask for `bestBid` and `bestAsk`.

**REST Equivalent:** `GET /api/prices?pair=BTC-USD`

**GraphQL Request:**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { getPrice(pair: \"BTC-USD\") { pair { base quote } bestBid bestAsk timestamp } }"
  }'
```

**Response:**
```json
{
  "data": {
    "getPrice": {
      "pair": { "base": "BTC", "quote": "USD" },
      "bestBid": 50100.50,
      "bestAsk": 50200.00,
      "timestamp": "2023-10-27T10:00:00Z"
    }
  }
}
```

### 2. Get Arbitrage Opportunities
Fetch a list of opportunities. You can choose to only retrieve the `profitPercentage` if that's all you care about.

**REST Equivalent:** `GET /api/arbitrage`

**GraphQL Request:**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { getArbitrageOpportunities { pair { base quote } profitPercentage buyExchange { id } sellExchange { id } } }"
  }'
```

## Testing with Postman / Insomnia

1.  Create a new Request.
2.  Set Method to **POST**.
3.  Set URL to `http://localhost:8080/graphql`.
4.  Go to the **Body** tab.
    *   **Postman:** Select **GraphQL**.
    *   **Insomnia:** Select **GraphQL Query**.
5.  Paste the query in the editor (no need to escape quotes like in cURL):

```graphql
query {
  getPrice(pair: "ETH-USD") {
    bestBid
    bestAskExchange {
      id
    }
  }
}
```

## The Schema

Our current schema supports:

*   `getPrice(pair: String!)`: Returns `ConsolidatedPrice`
*   `getArbitrageOpportunities`: Returns `[ArbitrageOpportunity]`

You can explore the full schema using a tool like **GraphiQL** (often available at `/graphiql` if enabled) to see exactly what fields are available.
