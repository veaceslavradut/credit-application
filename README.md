# Credit Application Platform

A Spring Boot backend for a credit application marketplace. It provides borrower application flows, bank administration features, and foundational infrastructure (CI/CD, Dockerized services, logging, health checks).

## Tech Stack & Versions
- Backend: Spring Boot 3.2.1
- Java: OpenJDK 21 (Temurin; Docker uses 21.0.1)
- Database: PostgreSQL 15.4 (Docker image)
- Cache: Redis 7.2.3 (Docker image)
- Build: Maven 3.8+
- CI/CD: GitHub Actions (lint, tests, build, integration tests, blue-green staging deploy simulation)
- Frontend: Next.js/React (planned - not yet implemented)

## Prerequisites
- JDK 21 (Temurin recommended)
- Maven 3.8+
- Docker Desktop and Docker Compose
- Optional: Node.js 20.x if working on the frontend (when implemented)

## Quick Start (Local)

### Option 1: Start Everything with Docker Compose (Recommended)
```bash
# Start all services (database, redis, backend, adminer)
docker compose up -d

# View logs
docker compose logs -f backend

# Stop all services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

### Option 2: Start Only Dependencies (for local development)
```bash
# Start PostgreSQL and Redis only
docker compose up -d postgres redis adminer

# Run backend locally with Maven
mvn spring-boot:run

# Or run the JAR (after building)
mvn clean package
java -jar target/credit-application-*.jar
```

### Verify Services
- **Backend Health**: http://localhost:8080/actuator/health
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/swagger-ui.html (if enabled)
- **Adminer (Database UI)**: http://localhost:8081
  - System: PostgreSQL
  - Server: postgres
  - Username: creditapp
  - Password: creditapp_dev_password
  - Database: credit_app
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

> **📚 For detailed Docker setup instructions, see [DOCKER_SETUP.md](DOCKER_SETUP.md)**

## Configuration (Environment Variables)
The application supports Spring Boot config via environment variables or `application-*.yml`.

Common local variables (already set in docker-compose):
- `SPRING_PROFILES_ACTIVE=local`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/credit_app`
- `SPRING_DATASOURCE_USERNAME=creditapp`
- `SPRING_DATASOURCE_PASSWORD=creditapp_dev_password`
- `SPRING_DATA_REDIS_HOST=redis`
- `SPRING_DATA_REDIS_PORT=6379`

Recommended additional variables for non-local environments:
- `SERVER_PORT` (default 8080)
- `LOG_LEVEL` (default INFO)
- `APP_VERSION` (e.g., 1.0.0)
- Database pool tuning via HikariCP (`SPRING_DATASOURCE_HIKARI_*`)

## Build, Test, Coverage
- Build: `mvn clean package`
- Unit tests: `mvn test`
- Integration tests + coverage: `mvn verify && mvn jacoco:report`
- Coverage report: `target/site/jacoco/index.html`

## Docker
- Build image: `docker build -t credit-application:local .`
- Run: `docker run -p 8080:8080 --env SPRING_PROFILES_ACTIVE=local credit-application:local`
- Compose (app + deps): `docker compose up -d`

## CI/CD Pipeline
- Location: `.github/workflows/ci-cd.yml`
- Triggers: push/pull_request to `main` and `develop`
- Stages: Lint  Unit Tests  Build  Integration Tests  Deploy to Staging (blue-green simulation)
- Artifacts: JAR, surefire test results, JaCoCo coverage report (30-day retention)

## Health Check
- Endpoint: `GET /api/health`
- Returns JSON status (database, redis, version)
- Used by Docker health checks and deployment validations

## Docker Compose Services

The application stack includes:

1. **postgres** - PostgreSQL 15.4 database
   - Port: 5432
   - Database: credit_app
   - User: creditapp
   - Password: creditapp_dev_password

2. **redis** - Redis 7.2.3 cache
   - Port: 6379
   - Password: creditapp_redis_password

3. **backend** - Spring Boot application
   - Port: 8080
   - Built from source using Dockerfile
   - Runs Flyway migrations on startup
   - Depends on postgres and redis

4. **adminer** - Database management UI
   - Port: 8081
   - Access PostgreSQL through web interface

5. **frontend** - (Placeholder - not yet implemented)
   - Will run on port 3000 when implemented

## Troubleshooting

### Port Conflicts
```bash
# Check what's using ports
netstat -ano | findstr "5432 6379 8080 8081 3000"

# Change ports in docker-compose.yml if needed
# Example: "8082:8080" to run backend on 8082
```

### Database Issues
```bash
# Check database health
docker compose ps
docker compose logs postgres

# Connect to database
docker exec -it credit-app-postgres psql -U creditapp -d credit_app

# Reset database (WARNING: deletes all data)
docker compose down -v
docker compose up -d
```

### Redis Issues
```bash
# Check Redis health
docker compose logs redis

# Connect to Redis CLI
docker exec -it credit-app-redis redis-cli -a creditapp_redis_password

# Test Redis connection
docker exec -it credit-app-redis redis-cli -a creditapp_redis_password PING
```

### Backend Build Issues
```bash
# Clean rebuild
docker compose down
docker compose build --no-cache backend
docker compose up -d backend

# View backend logs
docker compose logs -f backend

# Check backend health
curl http://localhost:8080/actuator/health
```

### Maven/Build Issues
```bash
# Clean Maven cache
mvn clean install -U

# Skip tests for faster build
mvn clean package -DskipTests

# View compilation errors
mvn clean compile
```

## Useful Commands

### Docker Compose
```bash
# Start all services
docker compose up -d

# Start specific services
docker compose up -d postgres redis

# View logs
docker compose logs -f backend
docker compose logs --tail=100 backend

# Restart a service
docker compose restart backend

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v

# View running services
docker compose ps

# Execute command in container
docker compose exec backend bash
```

### Database
```bash
# Database shell
docker exec -it credit-app-postgres psql -U creditapp -d credit_app

# Run SQL file
docker exec -i credit-app-postgres psql -U creditapp -d credit_app < script.sql

# Backup database
docker exec credit-app-postgres pg_dump -U creditapp credit_app > backup.sql

# Restore database
docker exec -i credit-app-postgres psql -U creditapp -d credit_app < backup.sql

# View tables
docker exec -it credit-app-postgres psql -U creditapp -d credit_app -c "\dt"
```

### Redis
```bash
# Redis CLI
docker exec -it credit-app-redis redis-cli -a creditapp_redis_password

# Check Redis keys
docker exec -it credit-app-redis redis-cli -a creditapp_redis_password KEYS "*"

# Clear Redis cache
docker exec -it credit-app-redis redis-cli -a creditapp_redis_password FLUSHALL
```

### Backend
```bash
# View backend logs
docker compose logs -f backend

# Tail last 100 lines
docker compose logs --tail=100 backend

# Execute command in backend container
docker compose exec backend bash

# Restart backend
docker compose restart backend
```

## License
Internal project. Do not distribute.
