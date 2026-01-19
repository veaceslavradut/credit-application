package com.creditapp.integration.borrower;

import com.creditapp.borrower.dto.ConsentRequestDTO;
import com.creditapp.borrower.dto.ConsentResponseDTO;
import com.creditapp.shared.model.ConsentType;
import com.creditapp.shared.model.GDPRConsent;
import com.creditapp.shared.repository.GDPRConsentRepository;
import com.creditapp.shared.service.GDPRConsentService;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GDPRConsentIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private GDPRConsentService consentService;
    
    @Autowired
    private GDPRConsentRepository consentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private UUID borrowerId;
    
    @BeforeEach
    void setUp() {
        User borrower = new User();
        borrower.setId(UUID.randomUUID());
        borrower.setEmail("borrower@test.com");
        borrower.setFirstName("Test");
        borrower.setLastName("Borrower");
        borrower.setPasswordHash("$2a$10$hashedpassword");
        borrower.setRole(UserRole.BORROWER);
        
        User saved = userRepository.save(borrower);
        borrowerId = saved.getId();
    }
    
    @Test
    void testGrantDataCollectionConsent() {
        ConsentRequestDTO request = ConsentRequestDTO.builder()
            .consentTypes(List.of(ConsentType.DATA_COLLECTION))
            .build();
        
        List<ConsentResponseDTO> responses = consentService.grantConsent(
            borrowerId,
            request.getConsentTypes(),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ConsentType.DATA_COLLECTION, responses.get(0).getConsentType());
        assertTrue(responses.get(0).getCurrentlyConsented());
    }
    
    @Test
    void testGrantMultipleConsents() {
        ConsentRequestDTO request = ConsentRequestDTO.builder()
            .consentTypes(List.of(ConsentType.DATA_COLLECTION, ConsentType.BANK_SHARING, ConsentType.MARKETING))
            .build();
        
        List<ConsentResponseDTO> responses = consentService.grantConsent(
            borrowerId,
            request.getConsentTypes(),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        assertEquals(3, responses.size());
        assertTrue(responses.stream().allMatch(ConsentResponseDTO::getCurrentlyConsented));
        assertTrue(responses.stream().map(ConsentResponseDTO::getConsentType).allMatch(
            t -> t == ConsentType.DATA_COLLECTION || t == ConsentType.BANK_SHARING || t == ConsentType.MARKETING
        ));
    }
    
    @Test
    void testWithdrawConsent() {
        consentService.grantConsent(
            borrowerId,
            List.of(ConsentType.DATA_COLLECTION),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        assertTrue(consentService.isConsentGiven(borrowerId, ConsentType.DATA_COLLECTION));
        
        ConsentResponseDTO response = consentService.withdrawConsent(borrowerId, ConsentType.DATA_COLLECTION);
        
        assertNotNull(response.getWithdrawnAt());
        assertFalse(response.getCurrentlyConsented());
        assertFalse(consentService.isConsentGiven(borrowerId, ConsentType.DATA_COLLECTION));
    }
    
    @Test
    void testGetCurrentConsents_FiltersWithdrawn() {
        consentService.grantConsent(
            borrowerId,
            List.of(ConsentType.DATA_COLLECTION, ConsentType.BANK_SHARING, ConsentType.MARKETING),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        assertEquals(3, consentService.getCurrentConsents(borrowerId).size());
        
        consentService.withdrawConsent(borrowerId, ConsentType.MARKETING);
        
        List<ConsentResponseDTO> current = consentService.getCurrentConsents(borrowerId);
        assertEquals(2, current.size());
        assertTrue(current.stream().allMatch(ConsentResponseDTO::getCurrentlyConsented));
        assertFalse(current.stream().anyMatch(r -> r.getConsentType() == ConsentType.MARKETING));
    }
    
    @Test
    void testConsentPersistenceInDatabase() {
        consentService.grantConsent(
            borrowerId,
            List.of(ConsentType.DATA_COLLECTION),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        List<GDPRConsent> consents = consentRepository.findAllByBorrowerId(borrowerId);
        
        assertEquals(1, consents.size());
        GDPRConsent consent = consents.get(0);
        assertEquals(borrowerId, consent.getBorrowerId());
        assertEquals(ConsentType.DATA_COLLECTION, consent.getConsentType());
        assertEquals("192.168.1.1", consent.getIpAddress());
        assertNull(consent.getWithdrawnAt());
    }
    
    @Test
    void testConsentImmutabilityAfterWithdrawal() {
        consentService.grantConsent(
            borrowerId,
            List.of(ConsentType.DATA_COLLECTION),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        GDPRConsent original = consentRepository.findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(
            borrowerId, ConsentType.DATA_COLLECTION
        ).orElse(null);
        
        assertNotNull(original);
        assertNull(original.getWithdrawnAt());
        
        consentService.withdrawConsent(borrowerId, ConsentType.DATA_COLLECTION);
        
        GDPRConsent withdrawn = consentRepository.findLatestByBorrowerIdAndType(
            borrowerId, ConsentType.DATA_COLLECTION
        ).orElse(null);
        
        assertNotNull(withdrawn);
        assertNotNull(withdrawn.getWithdrawnAt());
        assertEquals(original.getId(), withdrawn.getId());
        assertEquals(original.getConsentedAt(), withdrawn.getConsentedAt());
    }
}