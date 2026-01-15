# 5. INFRASTRUCTURE & DEPLOYMENT

## 5.1 Cloud Architecture

**Deployment Model:** Kubernetes on cloud (AWS EKS, GCP GKE, or DigitalOcean)

**Multi-Region:** Primary in Moldovan data center or EU region

**Containerization:**
- Docker images per service (user-service, application-service, offer-service)
- Base: Eclipse Temurin (OpenJDK 21)
- Multi-stage builds to minimize size
- Health check endpoints for k8s readiness probes

## 5.2 CI/CD Pipeline

**Tool:** GitHub Actions

**Stages:**
```
Commit → Lint → Unit Tests → Build → Security Scan → 
Deploy to Staging → Smoke Tests → Deploy to Prod
```

**Key Jobs:**
1. **Code Quality** — SonarQube scan, code coverage >80%
2. **Security** — OWASP dependency check, container scanning (Trivy)
3. **Build** — Maven build, Docker image push to registry
4. **Integration Tests** — Testcontainers with PostgreSQL, Redis
5. **Staging Deploy** — Blue-green deployment
6. **Production Deploy** — Canary deployment (10% → 50% → 100%)

---
