package com.creditapp.integration.auth;

import com.creditapp.CreditApplicationApplication;
import com.creditapp.auth.dto.BankRegistrationRequest;
import com.creditapp.auth.dto.BankRegistrationResponse;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.model.User;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CreditApplicationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BankRegistrationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerBank_ValidRequest_ReturnsCreatedWithBankDetails() {
        BankRegistrationRequest request = new BankRegistrationRequest();
        request.setBankName("Test Bank");
        request.setRegistrationNumber("TBK123456");
        request.setContactEmail("admin@testbank.com");
        request.setAdminFirstName("John");
        request.setAdminLastName("Doe");
        request.setAdminPassword("SecurePass123!");
        request.setAdminPasswordConfirm("SecurePass123!");
        request.setAdminPhone("+373-012-345-67");

        ResponseEntity<BankRegistrationResponse> response = restTemplate.postForEntity(
                "/api/auth/register-bank",
                request,
                BankRegistrationResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        var body = response.getBody();
        assertEquals("Test Bank", Objects.requireNonNull(body).getBankName());
        assertEquals("PENDING_ACTIVATION", body.getStatus());
        assertNotNull(body.getBankId());
        assertNotNull(body.getAdminUserId());

        Optional<Organization> savedBank = organizationRepository.findByRegistrationNumber("TBK123456");
        assertTrue(savedBank.isPresent());
        assertEquals("Test Bank", savedBank.get().getName());
        assertNotNull(savedBank.get().getActivationToken());

        Optional<User> savedAdmin = userRepository.findByEmail("admin@testbank.com");
        assertTrue(savedAdmin.isPresent());
        assertEquals("BANK_ADMIN", savedAdmin.get().getRole().name());
        assertEquals("John", savedAdmin.get().getFirstName());
    }

    @Test
    void registerBank_DuplicateRegistrationNumber_ReturnsConflict() {
        BankRegistrationRequest request1 = new BankRegistrationRequest();
        request1.setBankName("First Bank");
        request1.setRegistrationNumber("DUP123456");
        request1.setContactEmail("admin1@bank.com");
        request1.setAdminFirstName("Admin");
        request1.setAdminLastName("One");
        request1.setAdminPassword("SecurePass123!");
        request1.setAdminPasswordConfirm("SecurePass123!");
        request1.setAdminPhone("+373-012-345-67");

        restTemplate.postForEntity("/api/auth/register-bank", request1, BankRegistrationResponse.class);

        BankRegistrationRequest request2 = new BankRegistrationRequest();
        request2.setBankName("Second Bank");
        request2.setRegistrationNumber("DUP123456");
        request2.setContactEmail("admin2@bank.com");
        request2.setAdminFirstName("Admin");
        request2.setAdminLastName("Two");
        request2.setAdminPassword("SecurePass123!");
        request2.setAdminPasswordConfirm("SecurePass123!");
        request2.setAdminPhone("+373-012-345-68");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register-bank",
                request2,
                String.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void activateBank_ValidToken_UpdatesStatusToActive() {
        BankRegistrationRequest request = new BankRegistrationRequest();
        request.setBankName("Activation Test Bank");
        request.setRegistrationNumber("ACT123456");
        request.setContactEmail("activate@bank.com");
        request.setAdminFirstName("Test");
        request.setAdminLastName("Admin");
        request.setAdminPassword("SecurePass123!");
        request.setAdminPasswordConfirm("SecurePass123!");
        request.setAdminPhone("+373-012-345-69");

        restTemplate.postForEntity("/api/auth/register-bank", request, BankRegistrationResponse.class);

        Organization bank = organizationRepository.findByRegistrationNumber("ACT123456").orElseThrow();
        String activationToken = bank.getActivationToken();

        ResponseEntity<String> activationResponse = restTemplate.getForEntity(
                "/api/auth/activate?token=" + activationToken,
                String.class
        );

        assertEquals(HttpStatus.OK, activationResponse.getStatusCode());

        Organization activatedBank = organizationRepository.findByRegistrationNumber("ACT123456").orElseThrow();
        assertEquals("ACTIVE", activatedBank.getStatus().name());
        assertNotNull(activatedBank.getActivatedAt());
        assertNull(activatedBank.getActivationToken());
    }

    @Test
    void activateBank_InvalidToken_ReturnsNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/activate?token=INVALID_TOKEN_123",
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void activateBank_ExpiredToken_ReturnsBadRequest() {
        Organization bank = new Organization();
        bank.setName("Expired Bank");
        bank.setRegistrationNumber("EXP123456");
        bank.setTaxId("EXP123456");
        bank.setCountryCode("MD");
        bank.setActive(true);
        bank.setActivationToken("EXPIRED_TOKEN");
        bank.setActivationTokenExpiresAt(LocalDateTime.now().minusDays(1));
        organizationRepository.save(bank);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/activate?token=EXPIRED_TOKEN",
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void registerBank_InvalidEmail_ReturnsBadRequest() {
        BankRegistrationRequest request = new BankRegistrationRequest();
        request.setBankName("Invalid Email Bank");
        request.setRegistrationNumber("INV123456");
        request.setContactEmail("invalid-email");
        request.setAdminFirstName("Test");
        request.setAdminLastName("Admin");
        request.setAdminPassword("SecurePass123!");
        request.setAdminPasswordConfirm("SecurePass123!");
        request.setAdminPhone("+373-012-345-70");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register-bank",
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
