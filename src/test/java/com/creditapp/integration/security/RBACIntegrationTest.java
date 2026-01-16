package com.creditapp.integration.security;

import com.creditapp.auth.dto.LoginRequest;
import com.creditapp.auth.dto.LoginResponse;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.BankStatus;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.util.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class RBACIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public LoginRateLimiter loginRateLimiter() {
            LoginRateLimiter mock = mock(LoginRateLimiter.class);
            when(mock.checkRateLimit(anyString())).thenReturn(true);
            doNothing().when(mock).clearFailedAttempts(anyString());
            doNothing().when(mock).recordFailedAttempt(anyString());
            return mock;
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private String borrowerToken;
    private String bankAdminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        userRepository.deleteAll();
        organizationRepository.deleteAll();
        
        // Create an ACTIVE organization for bank admin
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName("Test Bank");
        org.setRegistrationNumber("TEST123");
        org.setTaxId("TAX123");
        org.setCountryCode("US");
        org.setStatus(BankStatus.ACTIVE);
        org = organizationRepository.save(org);
        
        User borrower = new User();
        borrower.setId(UUID.randomUUID());
        borrower.setEmail("borrower@test.com");
        borrower.setPasswordHash(passwordEncoder.encode("Password123!"));
        borrower.setFirstName("Test");
        borrower.setLastName("Borrower");
        borrower.setPhone("+1234567890");
        borrower.setRole(UserRole.BORROWER);
        userRepository.save(borrower);

        //Bank admin with active organization
        User bankAdmin = new User();
        bankAdmin.setId(UUID.randomUUID());
        bankAdmin.setEmail("admin@bank.com");
        bankAdmin.setPasswordHash(passwordEncoder.encode("Password123!"));
        bankAdmin.setFirstName("Test");
        bankAdmin.setLastName("Admin");
        bankAdmin.setPhone("+1234567891");
        bankAdmin.setRole(UserRole.BANK_ADMIN);
        bankAdmin.setOrganizationId(org.getId());
        userRepository.save(bankAdmin);

        LoginRequest borrowerLogin = new LoginRequest();
        borrowerLogin.setEmail("borrower@test.com");
        borrowerLogin.setPassword("Password123!");
        
        ResponseEntity<LoginResponse> borrowerResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                borrowerLogin,
                LoginResponse.class
        );
        
        if (borrowerResponse.getStatusCode() == HttpStatus.OK && borrowerResponse.getBody() != null) {
            borrowerToken = borrowerResponse.getBody().getAccessToken();
            System.out.println("Borrower token: " + (borrowerToken != null ? "present" : "null"));
        } else {
            System.out.println("Borrower login failed: " + borrowerResponse.getStatusCode());
        }

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@bank.com");
        adminLogin.setPassword("Password123!");
        
        ResponseEntity<LoginResponse> adminResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                adminLogin,
                LoginResponse.class
        );
        
        if (adminResponse.getStatusCode() == HttpStatus.OK && adminResponse.getBody() != null) {
            bankAdminToken = adminResponse.getBody().getAccessToken();
            System.out.println("Bank admin token: " + (bankAdminToken != null ? "present" : "null"));
        } else {
            System.out.println("Bank admin login failed: " + adminResponse.getStatusCode());
        }
    }

    @Test
    void testBorrowerCanAccessBorrowerEndpoints() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(borrowerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/borrower/applications",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testBorrowerCannotAccessBankEndpoints() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(borrowerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bank/queue",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testBankAdminCanAccessBankEndpoints() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bankAdminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bank/queue",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testBankAdminCannotAccessBorrowerEndpoints() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bankAdminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/borrower/applications",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUnauthenticatedAccessReturnsForbidden() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/borrower/applications",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testInvalidTokenReturns403() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/borrower/applications",
                HttpMethod.GET,
                request,
                String.class
        );

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void testAccessDeniedReturns403WithMessage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(borrowerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/bank/queue",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Forbidden") || response.getBody().contains("Access"));
    }

    @Test
    void testMultipleEndpointsWithSameRole() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(borrowerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> getResponse = restTemplate.exchange(
                baseUrl + "/api/borrower/applications",
                HttpMethod.GET,
                request,
                String.class
        );
        
        ResponseEntity<String> postResponse = restTemplate.exchange(
                baseUrl + "/api/borrower/applications",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
    }
}