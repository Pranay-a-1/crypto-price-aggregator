# Deployment Guide

This guide covers deploying the Crypto Price Aggregator frontend to GitHub Pages while running the backend locally or on a separate server.

## GitHub Pages Deployment

### Prerequisites

- Git repository pushed to GitHub
- GitHub account with repository access
- Backend server accessible via public URL (optional for testing, required for production)

### Step 1: Enable GitHub Pages

1. Go to your GitHub repository
2. Navigate to **Settings** → **Pages**
3. Under **Source**, select **GitHub Actions**

![GitHub Pages Source Selection](https://docs.github.com/assets/cb-47267/mw-1440/images/help/pages/publishing-source-drop-down.webp)

### Step 2: Deploy via GitHub Actions

The workflow is already configured in `.github/workflows/deploy.yml`. To trigger deployment:

```bash
git add .
git commit -m "Add GitHub Pages deployment"
git push origin main
```

The deployment will:
- Automatically trigger on every push to `main`
- Copy frontend files to GitHub Pages
- Deploy to `https://<username>.github.io/<repository-name>/`

You can also manually trigger deployment from the GitHub Actions tab.

### Step 3: Access Your Deployed Site

Once deployment completes (usually 1-2 minutes), your site will be available at:

```
https://<your-github-username>.github.io/<repository-name>/
```

Check the Actions tab for deployment status and the Pages settings for the exact URL.

---

## Backend Configuration

Since GitHub Pages only hosts static files, you need to run the backend separately.

### Option 1: Local Backend with Public URL (Current Setup)

To access your local backend from GitHub Pages, you need to expose it publicly using one of these tools:

#### Using ngrok (Recommended)

1. **Install ngrok:**
   ```bash
   # Download from https://ngrok.com/download or use:
   snap install ngrok
   ```

2. **Start your backend:**
   ```bash
   ./mvnw spring-boot:run
   # Or with Docker:
   sudo docker compose up
   ```

3. **Expose port 8080:**
   ```bash
   ngrok http 8080
   ```

4. **Configure frontend:**
   - Copy `frontend/config.example.js` to `frontend/config.js`
   - Update with your ngrok URL:
   ```javascript
   window.API_CONFIG = {
       API_BASE_URL: 'https://abc123.ngrok.io/api'
   };
   ```

5. **Commit and push:**
   ```bash
   git add frontend/config.js
   git commit -m "Add production API configuration"
   git push
   ```

> [!WARNING]
> **CORS Configuration Required**
> 
> You must enable CORS on your backend for your GitHub Pages domain. See the CORS section below.

#### Alternative Tools

- **localtunnel:** `npx localtunnel --port 8080`
- **serveo:** `ssh -R 80:localhost:8080 serveo.net`
- **cloudflared:** Cloudflare tunnel for more permanent setups

### Option 2: Cloud Hosting (Recommended for Production)

Deploy your backend to a cloud platform:

#### Render (Free Tier Available)

1. Create account at [render.com](https://render.com)
2. Create new **Web Service**
3. Connect your GitHub repository
4. Configure:
   - **Build Command:** `./mvnw clean package`
   - **Start Command:** `java -jar target/*.jar`
   - **Environment:** Add PostgreSQL and RabbitMQ services

#### Railway (Free Tier Available)

1. Create account at [railway.app](https://railway.app)
2. Create new project from GitHub repo
3. Add PostgreSQL and RabbitMQ services
4. Deploy automatically

#### Fly.io (Free Tier Available)

1. Install flyctl: `curl -L https://fly.io/install.sh | sh`
2. Create app: `fly launch`
3. Deploy: `fly deploy`

---

## CORS Configuration

### Update SecurityConfig.java

Add CORS configuration to allow requests from GitHub Pages:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        // ... rest of configuration
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Add your GitHub Pages URL
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:8080",
        "https://<your-github-username>.github.io"
    ));
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
}
```

> [!IMPORTANT]
> Replace `<your-github-username>` with your actual GitHub username in the CORS configuration.

---

## Environment Variables

For production deployment, you may want to configure these via environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/crypto_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672

# Server
SERVER_PORT=8080
```

---

## Troubleshooting

### CORS Errors

**Symptom:** Browser console shows CORS policy errors

**Solution:**
1. Verify CORS configuration includes your GitHub Pages URL
2. Restart backend server after CORS changes
3. Check browser network tab for actual error details

### API Connection Failed

**Symptom:** Frontend shows "Failed to fetch" errors

**Solution:**
1. Verify backend is running and accessible
2. Check `config.js` has correct API URL
3. Test API URL directly in browser: `https://your-api-url.com/api/prices/BTC/USD`
4. Verify GitHub Pages is using HTTPS (required for secure API calls)

### 401 Unauthorized

**Symptom:** Authentication errors in console

**Solution:**
1. Verify credentials in `app.js` match backend configuration
2. Check Basic Auth headers are being sent
3. Review SecurityConfig authentication settings

### ngrok URL Changes

**Symptom:** API stops working after restarting ngrok

**Solution:**
1. ngrok free tier generates new URLs on restart
2. Update `config.js` with new URL
3. Commit and push changes
4. Consider paid ngrok plan for static URLs or use cloud hosting

---

## Testing Deployment

### Local Testing

Before deploying to GitHub Pages, test locally:

```bash
# Start backend
./mvnw spring-boot:run

# Serve frontend (in another terminal)
cd frontend
python -m http.server 8000

# Visit http://localhost:8000
```

### Production Testing

After deployment:

1. Visit your GitHub Pages URL
2. Open browser DevTools (F12)
3. Check Console for errors
4. Verify Network tab shows successful API calls
5. Test all features: price refresh, arbitrage detection, chart updates

---

## Security Considerations

> [!CAUTION]
> **Production Security Checklist**

- [ ] Change default credentials (`user`/`password`) in `SecurityConfig.java`
- [ ] Update `AUTH_USERNAME` and `AUTH_PASSWORD` in `frontend/app.js`
- [ ] Use environment variables for sensitive configuration
- [ ] Enable HTTPS on backend (required for GitHub Pages)
- [ ] Restrict CORS to specific origins (not `*`)
- [ ] Consider OAuth2 for production authentication
- [ ] Never commit API keys or secrets to repository
- [ ] Add `config.js` to `.gitignore` if it contains secrets

---

## Continuous Deployment

The GitHub Actions workflow automatically deploys on every push to `main`. To customize:

### Deploy on Tag Only

```yaml
on:
  push:
    tags:
      - 'v*'
```

### Deploy Manually Only

```yaml
on:
  workflow_dispatch:
```

### Add Environment Variables

```yaml
env:
  API_URL: ${{ secrets.API_URL }}
```

---

## Next Steps

1. ✅ Deploy frontend to GitHub Pages
2. ✅ Configure backend with CORS
3. ✅ Test deployment end-to-end
4. 🔄 Set up monitoring and analytics
5. 🔄 Configure custom domain (optional)
6. 🔄 Add CI/CD for backend deployment

---

For more information, see:
- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [Spring Boot CORS Guide](https://spring.io/guides/gs/rest-service-cors/)
- [ngrok Documentation](https://ngrok.com/docs)
