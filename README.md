# Credit Application Platform

A Spring Boot backend for a credit application marketplace. It provides borrower application flows, bank administration features, and foundational infrastructure (CI/CD, Dockerized services, logging, health checks).

## Tech Stack & Versions
- Backend: Spring Boot 3.2.1
- Java: OpenJDK 21 (Temurin; Docker uses 21.0.1)
- Database: PostgreSQL 15.4 (Docker image)
- Cache: Redis 7.2.3 (Docker image)
- Build: Maven 3.8+
- CI/CD: GitHub Actions (lint, tests, build, integration tests, blue-green staging deploy simulation)

## Prerequisites
- JDK 21 (Temurin recommended)
- Maven 3.8+
- Docker Desktop and Docker Compose
- Optional: Node.js 20.x if working on the frontend (separate repo)

## Quick Start (Local)
1. Start dependencies (PostgreSQL, Redis):
   - `docker compose up -d postgres redis`
   - To run all services (including app container): `docker compose up -d`
2. Run the application (choose one):
   - Maven: `mvn spring-boot:run`
   - Jar (after build): `java -jar target/credit-application-*.jar`
   - Docker (built via Dockerfile): `docker compose up -d app`
3. Verify health: `curl http://localhost:8080/api/health`

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

## Troubleshooting
- Postgres connection errors: ensure `docker compose ps` shows healthy `credit-app-postgres`
- Redis unavailable: check `credit-app-redis` health; restart with `docker compose restart redis`
- Port conflicts: stop services on 5432/6379/8080 or change ports
- Maven cache issues in CI: rerun pipeline; local: `mvn -U clean package`
- Containers stuck: `docker compose down -v` to remove volumes (data loss warning), then `docker compose up -d`

## Useful Commands
- Logs (compose): `docker compose logs -f app`
- DB shell: `docker exec -it credit-app-postgres psql -U creditapp -d credit_app`
- Redis ping: `docker exec -it credit-app-redis redis-cli ping`

## License
Internal project. Do not distribute.
