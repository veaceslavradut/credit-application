# Deployment Runbook

Operational guide for staging and production deployments using the CI/CD pipeline and blue-green strategy.

## Environments
- Staging: `https://staging.credit-app.example.com`
- Production: `https://credit-app.example.com`

## Prerequisites
- CI pipeline green (lint, unit, integration tests passed)
- Access to container registry (GHCR) and deployment platform
- Health endpoint reachable: `/api/health`

## Staging Deployment (Blue-Green)
1. Trigger: Merge/push to `main` branch.
2. Pipeline stages: Lint  Unit  Build  Integration  Deploy to Staging.
3. Blue-Green flow:
   - BLUE serves traffic.
   - Deploy new version to GREEN.
   - Run health checks on GREEN:
     - `curl -sS https://staging.credit-app.example.com/api/health`
     - Verify `database=connected`, `redis=connected`, `version`.
   - Switch traffic to GREEN (100%).
   - Keep BLUE as rollback.
4. Post-deploy:
   - Monitor logs and metrics.
   - Verify key endpoints and basic flows.

## Production Deployment (Canary  Blue-Green)
1. Preconditions:
   - Staging validated.
   - Change approval received.
2. Canary rollout (recommended):
   - Route small % of traffic to new version.
   - Monitor errors, latency, health checks.
3. Full switch (blue-green):
   - Promote GREEN to 100% if canary is healthy.
   - Keep BLUE ready for rollback.

## Manual Deployment (Fallback)
If automated deploy is unavailable, use Docker image and your orchestrator:

- Build & push image:
```bash
docker build -t ghcr.io/<org>/credit-application:<version> .
docker push ghcr.io/<org>/credit-application:<version>
```
- Update deployment manifests to use the new tag.
- Apply changes (e.g., Kubernetes `kubectl apply -f deployment.yaml`).
- Validate with `/api/health`.

## Rollback Procedure
- Condition: Health checks fail or critical errors after switch.
- Actions:
  1. Route traffic back to BLUE environment.
  2. Investigate logs, metrics, and recent changes.
  3. If needed, redeploy last known-good artifact.
  4. Document incident and corrective actions.

## Validation Checklist
- Health endpoint returns `200 OK` and expected fields.
- DB migrations succeeded; app connects to Postgres.
- Redis connectivity validated.
- Error rates normal; latency within SLO.
- CI artifacts (tests, coverage) retained.

## Observability
- Logs: Structured JSON via Logback (see `logback-spring.xml`).
- Metrics/Health: Spring Boot Actuator.
- Coverage: JaCoCo report in CI artifacts.

## Incident Response Contacts
- On-Call Engineering: #on-call-backend (Slack)
- Dev Lead: Jane Doe (email placeholder)
- Scrum Master: Bob (from change log)
- Ops Support: ops@example.com

## Notes
- Current CI deploy step simulates blue-green; integrate with your infra (e.g., Kubernetes, load balancer) for actual traffic switching.
