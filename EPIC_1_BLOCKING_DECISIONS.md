# Epic 1 Blocking Decisions Resolution Guide

## Executive Summary

This document resolves 5 critical architectural decisions blocking Epic 1 story refinement.

---

## DECISION 1: Async Email Service Framework

### Status: **CRITICAL** (Stories 1.3, 1.4 depend on this)

### Recommendation: **Option B (Spring Events) for MVP → Option C (Message Queue) for Production**

**Spring Events (MVP):**
- Clean architecture: event-driven pattern
- Zero additional infrastructure
- Easy to replace with message queue later
- Sufficient for internal testing

```java
public class UserRegisteredEvent extends ApplicationEvent {
    private UUID userId;
    private String email;
    private String userName;
}

@Service
public class UserRegistrationService {
    @Autowired ApplicationEventPublisher eventPublisher;
    
    public void registerBorrower(RegistrationRequest request) {
        // validate, create user
        eventPublisher.publishEvent(new UserRegisteredEvent(user));
    }
}

@Service
@Async
public class EmailNotificationListener {
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        sendGrid.sendWelcomeEmail(event.getEmail(), event.getUserName());
    }
}
```

**Production Upgrade Path (Message Queue):**
Replace @EventListener with RabbitMQ consumer for durability and retry support.

### Story Impact:
- **Story 1.3 (User Registration)**: Publish UserRegisteredEvent
- **Story 1.4 (Bank Activation)**: Publish BankRegisteredEvent and BankActivatedEvent

---

## DECISION 2: Redis-Backed vs In-Memory Storage

### Status: **CRITICAL** (Stories 1.5, 1.3 depend on this)

### Recommendation: **Redis (Primary) + In-Memory Fallback for Dev**

**Why Redis:**
- Already provisioned in Story 1.0 infrastructure
- Multi-instance support for scaling
- Compliance durability requirement
- Spring Boot auto-configures with Lettuce client

**Use Cases:**
1. Failed login tracking (5 attempts, 15-min lockout)
2. Refresh token storage (7-day TTL)
3. Rate limiting state (10 registrations/hour per IP)

```java
@Service
public class LoginAttemptService {
    @Autowired RedisTemplate<String, Integer> redisTemplate;
    
    public void recordFailedLogin(String email) {
        String key = "failed_login:" + email;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            redisTemplate.opsForValue().set(key, 1, Duration.ofMinutes(15));
        } else {
            redisTemplate.opsForValue().set(key, attempts + 1, Duration.ofMinutes(15));
        }
    }
    
    public boolean isAccountLocked(String email) {
        Integer attempts = (Integer) redisTemplate.opsForValue()
            .get("failed_login:" + email);
        return attempts != null && attempts >= 5;
    }
}
```

### Configuration:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: ""
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
```

### Story Impact:
- **Story 1.5 (JWT)**: Refresh token storage + failed login tracking
- **Story 1.3 (Registration)**: Rate limiting per IP address

---

## DECISION 3: Audit Log Archival Strategy

### Status: **CRITICAL** (Story 1.7 depends on this)

### Recommendation: **Option B (Archive Table) for MVP → Option C (S3) for Production**

**MVP Phase (Story 1.7):**
- Archive table in PostgreSQL for logs > 3 years
- Daily scheduled job moves old logs
- All data in single database (simpler ops)

```sql
CREATE TABLE audit_logs_archive (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100),
    entity_id UUID,
    action VARCHAR(50),
    actor_id UUID,
    created_at TIMESTAMP,
    archived_at TIMESTAMP DEFAULT NOW()
);

-- Partitioned by year for query performance
CREATE TABLE audit_logs_2024 PARTITION OF audit_logs
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

```java
@Service
public class AuditLogArchivalService {
    
    @Scheduled(cron = "0 0 2 * * ?")  // Daily at 2 AM
    public void archiveOldLogs() {
        LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(3);
        auditLogRepository.moveToArchive(threeYearsAgo);
        logger.info("Archived logs older than {}", threeYearsAgo);
    }
}
```

**Production Phase (Future Story):**
- S3 for long-term storage (cost-effective at scale)
- Keep recent 3 years in PostgreSQL
- Elasticsearch for compliance analytics (optional)

### Story Impact:
- **Story 1.7 (Audit)**: Implement archive table + daily archival job
- **Story 5.8 (Compliance)**: Query both current and archived logs for reports

---

## DECISION 4: JWT Library Version

### Status: **IMPORTANT** (Story 1.5 depends on this)

### Recommendation: **Pin JJWT 0.11.5 for MVP**

**Rationale:**
- 0.11.5 is proven in production
- 0.12.x has breaking changes (API refactoring)
- Migration effort not worth Sprint 2 delay
- Future: Schedule 0.12.x migration as separate story

### Configuration (pom.xml):
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## DECISION 5: Entity Audit Listener vs AOP

### Status: **IMPORTANT** (Story 1.7 depends on this)

### Recommendation: **Hybrid Approach (JPA for entities + AOP for business)**

**JPA @EntityListener** for entity persistence events:
- USER_REGISTERED on User creation
- ORGANIZATION_CREATED on Organization creation
- BANK_ACTIVATED on status change

```java
@Entity
@EntityListeners(UserAuditListener.class)
public class User {
    // ...
}

public class UserAuditListener {
    @PostPersist
    public void onUserCreated(User user) {
        auditService.logAction("USER_REGISTERED", 
            user.getId(), user.getRole());
    }
}
```

**Spring @Aspect + @Audit** for business domain events:
- APPLICATION_SUBMITTED
- OFFER_ACCEPTED
- PASSWORD_CHANGED

```java
@Service
public class ApplicationService {
    @Audit(action = "APPLICATION_SUBMITTED")
    public void submitApplication(UUID appId) {
        // Business logic - audit captured automatically
    }
}

@Aspect
@Component
public class BusinessAuditAspect {
    @Around("@annotation(businessAudit)")
    public Object auditBusinessEvent(ProceedingJoinPoint pjp, 
            BusinessAudit businessAudit) throws Throwable {
        Object result = pjp.proceed();
        auditService.logAction(businessAudit.action(), 
            extractEntityId(result), businessAudit.reason());
        return result;
    }
}
```

**Benefits:**
- Clear separation: persistence vs business logic
- Flexible: easy to add/remove business auditing
- Testable: can mock aspects independently
- Scalable: AOP grows with features

---

## Summary: Decisions → Story Updates

| Decision | Choice | Story Impact | Risk |
|----------|--------|--------------|------|
| Email Framework | Spring Events → Message Queue | 1.3, 1.4 publish events | LOW |
| Redis Storage | Redis + fallback | 1.3, 1.5 use Redis | LOW |
| Audit Archival | Archive table → S3 | 1.7 implement archival | LOW |
| JWT Library | JJWT 0.11.5 (stable) | 1.5 pin version | NONE |
| Audit Pattern | JPA + AOP hybrid | 1.7 mixed pattern | LOW |

---

## Implementation Ready

✅ All blocking decisions resolved  
✅ Epic 1 stories unblocked for development  
✅ Compliance requirements addressed (GDPR, Moldovan law)  
✅ Upgrade paths documented for production hardening

**Next Step:** Revise high-risk stories (1.3, 1.5, 1.6, 1.7) with these decisions baked in.
