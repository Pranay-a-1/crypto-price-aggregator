---
description: Run SonarQube code quality analysis
---

# SonarQube Analysis Workflow

This workflow guides you through running SonarQube code quality analysis on the Crypto Price Aggregator project.

## Prerequisites

- Docker and Docker Compose installed
- Maven installed (or use `./mvnw`)
- At least 4GB of available RAM

## Steps

### 1. Start SonarQube Service

Start SonarQube and its database:

```bash
sudo docker compose up -d sonarqube-db sonarqube
```

Wait for SonarQube to fully start (this may take 1-2 minutes on first run). You can monitor the logs:

```bash
sudo docker compose logs -f sonarqube
```

Look for the message: `SonarQube is operational`

### 2. Access SonarQube Dashboard

Open your browser and navigate to:

```
http://localhost:9000
```

**Default credentials:**
- Username: `admin`
- Password: `admin`

You'll be prompted to change the password on first login.

### 3. Generate Authentication Token (First Time Only)

1. Login to SonarQube dashboard
2. Go to **My Account** (top right) → **Security**
3. Generate a new token:
   - Name: `maven-analysis`
   - Type: `User Token`
   - Expires: Choose appropriate expiration
4. **Copy the token** - you'll need it for the next step

### 4. Run Code Analysis

Run the Maven build with SonarQube analysis:

**With authentication token (recommended):**

```bash
./mvnw clean verify sonar:sonar -Dsonar.token=YOUR_TOKEN_HERE
```

**Without authentication (only works on first run before password change):**

```bash
./mvnw clean verify sonar:sonar
```

The analysis includes:
- Code quality metrics
- Code coverage (via JaCoCo)
- Bug detection
- Security vulnerability scanning
- Code smell identification
- Technical debt calculation

### 5. View Results

Once the analysis completes, view the results in the SonarQube dashboard:

```
http://localhost:9000/dashboard?id=crypto-price-aggregator
```

You'll see:
- **Bugs**: Code defects that should be fixed
- **Vulnerabilities**: Security issues
- **Code Smells**: Maintainability issues
- **Coverage**: Test coverage percentage
- **Duplications**: Duplicated code blocks
- **Security Hotspots**: Security-sensitive code to review

### 6. Stop SonarQube (Optional)

When you're done, you can stop SonarQube to free up resources:

```bash
sudo docker compose stop sonarqube sonarqube-db
```

Or remove containers entirely:

```bash
sudo docker compose down sonarqube sonarqube-db
```

**Note:** Volumes persist, so your analysis history is saved.

## Tips

- **CI/CD Integration**: Add SonarQube analysis to your CI pipeline
- **Quality Gates**: Configure quality gates in SonarQube to enforce standards
- **Regular Analysis**: Run analysis after major changes or before releases
- **Coverage Improvement**: Aim for >80% code coverage
- **Fix Issues**: Prioritize bugs and vulnerabilities over code smells

## Troubleshooting

### SonarQube won't start

Check container logs:
```bash
sudo docker compose logs sonarqube
```

Common issues:
- Insufficient memory (needs 2-4GB)
- Port 9000 already in use
- Database connection issues

### Analysis fails

1. Ensure SonarQube is fully started (`http://localhost:9000/api/system/status` should return `"status":"UP"`)
2. Check if authentication token is valid
3. Verify Maven build succeeds: `./mvnw clean verify`

### Out of memory error

Increase Docker memory limits in `docker-compose.yml` or Docker Desktop settings.
