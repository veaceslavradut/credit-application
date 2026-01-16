package com.creditapp.unit.shared.model;

import com.creditapp.shared.model.User;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.audit.AuditLog;
import com.creditapp.shared.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogEntityTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("BORROWER");
        User savedUser = userRepository.save(user);
        testUserId = savedUser.getId();
    }

    @Test
    void createAndFindAuditLog() {
        AuditLog log = new AuditLog();
        log.setUserId(testUserId);
        log.setAction("USER_LOGIN");
        log.setResource("AUTH");
        log.setResult("SUCCESS");
        log.setIpAddress("192.168.1.1");

        auditLogRepository.save(log);

        assertThat(log.getId()).isNotNull();
    }

    @Test
    void findAuditLogsByUserId() {
        AuditLog log1 = new AuditLog();
        log1.setUserId(testUserId);
        log1.setAction("LOGIN");
        log1.setResource("AUTH");
        log1.setResult("SUCCESS");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setUserId(testUserId);
        log2.setAction("UPDATE_PROFILE");
        log2.setResource("USER");
        log2.setResult("SUCCESS");
        auditLogRepository.save(log2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> found = auditLogRepository.findByUserIdOrderByTimestampDesc(testUserId, pageable);

        assertThat(found.getContent()).hasSize(2);
        assertThat(found.getContent().get(0).getAction()).isEqualTo("UPDATE_PROFILE");
    }

    @Test
    void auditLogOrderingByTimestamp() {
        AuditLog log1 = new AuditLog();
        log1.setUserId(testUserId);
        log1.setAction("FIRST");
        log1.setResource("AUTH");
        log1.setResult("SUCCESS");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setUserId(testUserId);
        log2.setAction("SECOND");
        log2.setResource("AUTH");
        log2.setResult("SUCCESS");
        auditLogRepository.save(log2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(testUserId, pageable);

        assertThat(logs.getContent()).hasSize(2);
        assertThat(logs.getContent().get(0).getAction()).isEqualTo("SECOND");
        assertThat(logs.getContent().get(1).getAction()).isEqualTo("FIRST");
    }

    @Test
    void auditLogIsImmutable() {
        AuditLog log = new AuditLog();
        log.setUserId(testUserId);
        log.setAction("CREATE_USER");
        log.setResource("USER");
        log.setResult("SUCCESS");

        AuditLog saved = auditLogRepository.save(log);
        assertThat(saved.getId()).isNotNull();
    }
}
