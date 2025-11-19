# Security Architecture - Crypto Price Aggregator

## Overview

The Cryptocurrency Price Aggregator implements **OAuth2 Resource Server** pattern for API security. This document explains the security architecture, configuration, and usage.

---

## Architecture Pattern: OAuth2 Resource Server

### What is it?

This application **does NOT handle user authentication** (no login forms, no user database). Instead, it validates JWT tokens issued by an **external Identity Provider (IdP)**.

### Flow Diagram

```
┌─────────┐                    ┌──────────────┐                    ┌─────────────┐
│  Client │                    │   IdP        │                    │     API     │
│         │                    │ (Okta/Auth0) │                    │ (Our App)   │
└────┬────┘                    └──────┬───────┘                    └──────┬──────┘
     │                                │                                   │
     │ 1. POST /oauth2/token          │                                   │
     │ ──────────────────────────────>│                                   │
     │                                │                                   │
     │ 2. JWT Token                   │                                   │
     │ <──────────────────────────────│                                   │
     │                                │                                   │
     │ 3. GET /api/v1/price/BTC-USD   │                                   │
     │    Authorization: Bearer <jwt> │                                   │
     │ ───────────────────────────────────────────────────────────────>  │
     │                                │                                   │
     │                                │  4. Validate JWT signature        │
     │                                │     (fetch public keys if needed) │
     │                                │ <─────────────────────────────────│
     │                                │                                   │
     │                                │  5. JWT valid ✓                   │
     │                                │ ─────────────────────────────────>│
     │                                │                                   │
     │ 6. 200 OK { price data }       │                                   │
     │ <──────────────────────────────────────────────────────────────── │
     │                                │                                   │
```

### Key Components

1. **Identity Provider (IdP)**: External service that authenticates users and issues JWTs
    - Examples: Okta, Auth0, Keycloak, AWS Cognito, Azure AD

2. **JWT (JSON Web Token)**: Signed token containing user claims
    - Header: Algorithm and type
    - Payload: Claims (user ID, roles, expiration, etc.)
    - Signature: Cryptographic signature to verify authenticity

3. **JWK Set URI**: Public endpoint exposing IdP's public keys
    - Our app fetches these keys to verify JWT signatures
    - Keys are cached to avoid repeated network calls

4. **Resource Server (Our App)**: Validates JWTs and protects endpoints
    - Does NOT issue tokens
    - Does NOT authenticate users
    - ONLY validates tokens

---

## Configuration

### application.properties

```properties
# Primary configuration for JWT validation
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://your-idp.com/.well-known/jwks.json

# Optional: Validate JWT issuer claim
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-idp.com
```

### Environment-Specific Configuration

#### Development (application-dev.properties)
```properties
# Use local mock IdP or disable security for testing
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/oauth2/jwks
```

#### Production (application-prod.properties)
```properties
# Use environment variable for production IdP
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${JWT_JWKS_URI}
spring.security.oauth2.resourceserver.jwt.issuer-uri=${JWT_ISSUER_URI}
```

---

## Security Filter Chain

### What's Protected?

| Endpoint Pattern | Access Policy |
|-----------------|---------------|
| `/api/v1/**` | ✅ Authenticated users only |
| `/actuator/health` | ✅ Public access (for load balancers) |
| `/actuator/**` | ✅ Authenticated users only |
| All other requests | ❌ Denied |

### Session Management

- **Stateless**: No server-side sessions
- Each request must include a valid JWT
- No session cookies or JSESSIONID

### CSRF Protection

- **Disabled**: Not needed for stateless JWT APIs
- CSRF is designed for browser-based, session-based apps

---

## Usage Examples

### 1. Obtain a JWT Token

**Option A: Using Okta**
```bash
curl -X POST https://dev-123456.okta.com/oauth2/default/v1/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "scope=api:read"
```

**Option B: Using Auth0**
```bash
curl -X POST https://your-tenant.auth0.com/oauth/token \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "YOUR_CLIENT_ID",
    "client_secret": "YOUR_CLIENT_SECRET",
    "audience": "https://api.cryptoarb.com",
    "grant_type": "client_credentials"
  }'
```

Response:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 2. Call Protected API with JWT

```bash
# Store token in a variable
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Call the API
curl -X GET http://localhost:8080/api/v1/price/BTC-USD \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
```

### 3. Expected Responses

**Success (200 OK)**
```json
{
  "pair": {
    "base": "BTC",
    "quote": "USD"
  },
  "timestamp": "2025-11-19T10:00:00Z",
  "bestBid": 50000.00,
  "bestBidExchange": {
    "exchangeId": "kraken"
  },
  "bestAsk": 50001.00,
  "bestAskExchange": {
    "exchangeId": "coinbase"
  }
}
```

**Unauthorized (401)**
```json
{
  "timestamp": "2025-11-19T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/v1/price/BTC-USD"
}
```

---

## Testing Security

### Unit Tests (Mocked Security)

```java
@WebMvcTest(PriceController.class)
class PriceControllerTest {
    
    @Test
    @WithMockUser // Mocks authentication
    void shouldReturnPriceWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/price/BTC-USD"))
                .andExpect(status().isOk());
    }
}
```

### Integration Tests (Security Enabled)

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    
    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/price/BTC-USD"))
                .andExpect(status().isUnauthorized());
    }
}
```

### Disabling Security for Tests

In `src/test/resources/application.properties`:
```properties
# Use a dummy JWK Set URI for tests
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/oauth2/jwks

# Or exclude security auto-configuration
spring.autoconfigure.exclude=\
org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

---

## Common IdP Configurations

### Okta

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://dev-123456.okta.com/oauth2/default/v1/keys
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dev-123456.okta.com/oauth2/default
```

### Auth0

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://your-tenant.auth0.com/.well-known/jwks.json
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-tenant.auth0.com/
```

### Keycloak

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/cryptoarb/protocol/openid-connect/certs
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/cryptoarb
```

### AWS Cognito

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.{region}.amazonaws.com/{userPoolId}
```

---

## Advanced: Custom JWT Validation

If you need custom claim validation, create a custom `JwtDecoder` bean:

```java
@Configuration
public class JwtConfig {
    
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri("https://your-idp.com/.well-known/jwks.json")
            .build();
        
        // Add custom validators
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            new JwtIssuerValidator("https://your-idp.com"),
            new CustomClaimValidator() // Your custom logic
        ));
        
        return decoder;
    }
}
```

Example custom validator:
```java
public class CustomClaimValidator implements OAuth2TokenValidator<Jwt> {
    
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Check for required custom claim
        String tenantId = jwt.getClaim("tenant_id");
        
        if (tenantId == null || !isValidTenant(tenantId)) {
            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Invalid tenant_id claim", null)
            );
        }
        
        return OAuth2TokenValidatorResult.success();
    }
}
```

---

## Security Checklist

- [ ] Configure production IdP JWK Set URI via environment variable
- [ ] Enable HTTPS/TLS in production (server.ssl.enabled=true)
- [ ] Set appropriate CORS policies for frontend (Phase 12)
- [ ] Implement rate limiting to prevent abuse
- [ ] Enable security headers (X-Frame-Options, X-Content-Type-Options)
- [ ] Set up monitoring/alerts for authentication failures
- [ ] Rotate IdP client secrets regularly
- [ ] Implement proper logging for security events
- [ ] Test with various JWT scenarios (expired, malformed, invalid signature)

---

## Troubleshooting

### 401 Unauthorized with valid token

1. Check JWT expiration: `jwt.io` → paste token → check "exp" claim
2. Verify issuer matches: Token's "iss" claim must match configured issuer URI
3. Check audience claim: Some IdPs require specific "aud" claim
4. Verify JWK Set URI is reachable: `curl https://your-idp.com/.well-known/jwks.json`

### Application startup fails

```
Unable to resolve Configuration with the provided Issuer
```

**Solution**: Check that `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` is correct and reachable.

### Tests fail with "No bean named 'springSecurityFilterChain'"

**Solution**: Add security test dependency and configure test properties:
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---
