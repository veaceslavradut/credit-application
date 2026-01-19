package com.creditapp.unit.shared;

import com.creditapp.borrower.dto.ConsentResponseDTO;
import com.creditapp.shared.model.GDPRConsent;
import com.creditapp.shared.model.ConsentType;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.repository.GDPRConsentRepository;
import com.creditapp.shared.service.AuditService;
import com.creditapp.shared.service.GDPRConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GDPRConsentServiceTest {
    
    @Mock
    private GDPRConsentRepository consentRepository;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private GDPRConsentService consentService;
    
    private UUID borrowerId;
    private UUID consentId;
    
    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        consentId = UUID.randomUUID();
    }
    
    @Test
    void testGrantConsentWithValidState() {
        LocalDateTime now = LocalDateTime.now();
        GDPRConsent consent = GDPRConsent.builder()
            .id(consentId)
            .borrowerId(borrowerId)
            .consentType(ConsentType.DATA_COLLECTION)
            .consentedAt(now)
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .version(1)
            .build();
        
        when(consentRepository.save(any(GDPRConsent.class))).thenReturn(consent);
        
        List<ConsentResponseDTO> responses = consentService.grantConsent(
            borrowerId,
            List.of(ConsentType.DATA_COLLECTION),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ConsentType.DATA_COLLECTION, responses.get(0).getConsentType());
        assertTrue(responses.get(0).getCurrentlyConsented());
        
        verify(consentRepository, times(1)).save(any(GDPRConsent.class));
        verify(auditService, times(1)).logActionWithValues(eq("GDPRConsent"), eq(borrowerId), eq(AuditAction.CONSENT_GRANTED), any(), any());
    }
    
    @Test
    void testGrantMultipleConsentTypes() {
        LocalDateTime now = LocalDateTime.now();
        GDPRConsent consent1 = GDPRConsent.builder()
            .id(UUID.randomUUID())
            .borrowerId(borrowerId)
            .consentType(ConsentType.DATA_COLLECTION)
            .consentedAt(now)
            .version(1)
            .build();
        
        GDPRConsent consent2 = GDPRConsent.builder()
            .id(UUID.randomUUID())
            .borrowerId(borrowerId)
            .consentType(ConsentType.BANK_SHARING)
            .consentedAt(now)
            .version(1)
            .build();
        
        when(consentRepository.save(any(GDPRConsent.class)))
            .thenReturn(consent1)
            .thenReturn(consent2);
        
        List<ConsentResponseDTO> responses = consentService.grantConsent(
            borrowerId,
            List.of(ConsentType.DATA_COLLECTION, ConsentType.BANK_SHARING),
            "192.168.1.1",
            "Mozilla/5.0"
        );
        
        assertEquals(2, responses.size());
        assertEquals(ConsentType.DATA_COLLECTION, responses.get(0).getConsentType());
        assertEquals(ConsentType.BANK_SHARING, responses.get(1).getConsentType());
        
        verify(consentRepository, times(2)).save(any(GDPRConsent.class));
        verify(auditService, times(2)).logActionWithValues(eq("GDPRConsent"), eq(borrowerId), eq(AuditAction.CONSENT_GRANTED), any(), any());
    }
    
    @Test
    void testWithdrawConsentWithValidState() {
        LocalDateTime consentedAt = LocalDateTime.now().minusDays(1);
        GDPRConsent consent = GDPRConsent.builder()
            .id(consentId)
            .borrowerId(borrowerId)
            .consentType(ConsentType.DATA_COLLECTION)
            .consentedAt(consentedAt)
            .withdrawnAt(null)
            .version(1)
            .build();
        
        when(consentRepository.findLatestByBorrowerIdAndType(borrowerId, ConsentType.DATA_COLLECTION))
            .thenReturn(Optional.of(consent));
        
        GDPRConsent updated = GDPRConsent.builder()
            .id(consentId)
            .borrowerId(borrowerId)
            .consentType(ConsentType.DATA_COLLECTION)
            .consentedAt(consentedAt)
            .withdrawnAt(LocalDateTime.now())
            .version(1)
            .build();
        
        when(consentRepository.save(any(GDPRConsent.class))).thenReturn(updated);
        
        ConsentResponseDTO response = consentService.withdrawConsent(borrowerId, ConsentType.DATA_COLLECTION);
        
        assertNotNull(response);
        assertEquals(ConsentType.DATA_COLLECTION, response.getConsentType());
        assertNotNull(response.getWithdrawnAt());
        assertFalse(response.getCurrentlyConsented());
        
        verify(consentRepository, times(1)).findLatestByBorrowerIdAndType(borrowerId, ConsentType.DATA_COLLECTION);
        verify(consentRepository, times(1)).save(any(GDPRConsent.class));
        verify(auditService, times(1)).logActionWithValues(eq("GDPRConsent"), eq(borrowerId), eq(AuditAction.CONSENT_WITHDRAWN), any(), any());
    }
    
    @Test
    void testIsConsentGiven_WhenConsentExists() {
        GDPRConsent consent = GDPRConsent.builder()
            .id(consentId)
            .borrowerId(borrowerId)
            .consentType(ConsentType.BANK_SHARING)
            .consentedAt(LocalDateTime.now())
            .withdrawnAt(null)
            .build();
        
        when(consentRepository.findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(borrowerId, ConsentType.BANK_SHARING))
            .thenReturn(Optional.of(consent));
        
        boolean result = consentService.isConsentGiven(borrowerId, ConsentType.BANK_SHARING);
        
        assertTrue(result);
        verify(consentRepository, times(1)).findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(borrowerId, ConsentType.BANK_SHARING);
    }
    
    @Test
    void testIsConsentGiven_WhenConsentWithdrawn() {
        when(consentRepository.findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(borrowerId, ConsentType.MARKETING))
            .thenReturn(Optional.empty());
        
        boolean result = consentService.isConsentGiven(borrowerId, ConsentType.MARKETING);
        
        assertFalse(result);
        verify(consentRepository, times(1)).findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(borrowerId, ConsentType.MARKETING);
    }
    
    @Test
    void testGetCurrentConsents() {
        LocalDateTime now = LocalDateTime.now();
        GDPRConsent consent1 = GDPRConsent.builder()
            .borrowerId(borrowerId)
            .consentType(ConsentType.DATA_COLLECTION)
            .consentedAt(now)
            .withdrawnAt(null)
            .build();
        
        GDPRConsent consent2 = GDPRConsent.builder()
            .borrowerId(borrowerId)
            .consentType(ConsentType.BANK_SHARING)
            .consentedAt(now)
            .withdrawnAt(null)
            .build();
        
        GDPRConsent consentWithdrawn = GDPRConsent.builder()
            .borrowerId(borrowerId)
            .consentType(ConsentType.MARKETING)
            .consentedAt(now.minusDays(1))
            .withdrawnAt(now)
            .build();
        
        when(consentRepository.findAllByBorrowerId(borrowerId))
            .thenReturn(List.of(consent1, consent2, consentWithdrawn));
        
        List<ConsentResponseDTO> responses = consentService.getCurrentConsents(borrowerId);
        
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(ConsentResponseDTO::getCurrentlyConsented));
        assertFalse(responses.stream().anyMatch(r -> r.getConsentType() == ConsentType.MARKETING));
        
        verify(consentRepository, times(1)).findAllByBorrowerId(borrowerId);
    }
}