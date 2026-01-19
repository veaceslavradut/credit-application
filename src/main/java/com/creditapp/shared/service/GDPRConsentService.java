package com.creditapp.shared.service;

import com.creditapp.borrower.dto.ConsentResponseDTO;
import com.creditapp.shared.model.GDPRConsent;
import com.creditapp.shared.model.ConsentType;
import com.creditapp.shared.model.AuditAction;
import com.creditapp.shared.repository.GDPRConsentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GDPRConsentService {
    
    private final GDPRConsentRepository consentRepository;
    private final AuditService auditService;
    
    public List<ConsentResponseDTO> grantConsent(UUID borrowerId, List<ConsentType> types, String ipAddress, String userAgent) {
        log.info("Granting consent for borrower {} with types {}", borrowerId, types);
        
        List<GDPRConsent> consents = types.stream()
            .map(type -> {
                GDPRConsent consent = GDPRConsent.builder()
                    .borrowerId(borrowerId)
                    .consentType(type)
                    .consentedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .version(1)
                    .build();
                
                GDPRConsent saved = consentRepository.save(consent);
                
                auditService.logActionWithValues(
                    "GDPRConsent",
                    borrowerId,
                    AuditAction.CONSENT_GRANTED,
                    null,
                    java.util.Map.of("consentType", type.toString(), "ipAddress", ipAddress)
                );
                
                log.info("Consent granted: {} for borrower {}", type, borrowerId);
                return saved;
            })
            .collect(Collectors.toList());
        
        return consents.stream()
            .map(this::toConsentResponse)
            .collect(Collectors.toList());
    }
    
    public ConsentResponseDTO withdrawConsent(UUID borrowerId, ConsentType type) {
        log.info("Withdrawing consent for borrower {} with type {}", borrowerId, type);
        
        GDPRConsent consent = consentRepository.findLatestByBorrowerIdAndType(borrowerId, type)
            .orElseThrow(() -> {
                log.error("Consent not found for borrower {} with type {}", borrowerId, type);
                return new RuntimeException("Consent not found");
            });
        
        consent.setWithdrawnAt(LocalDateTime.now());
        GDPRConsent updated = consentRepository.save(consent);
        
        auditService.logActionWithValues(
            "GDPRConsent",
            borrowerId,
            AuditAction.CONSENT_WITHDRAWN,
            null,
            java.util.Map.of("consentType", type.toString())
        );
        
        log.info("Consent withdrawn: {} for borrower {}", type, borrowerId);
        
        return toConsentResponse(updated);
    }
    
    public boolean isConsentGiven(UUID borrowerId, ConsentType type) {
        return consentRepository.findByBorrowerIdAndConsentTypeAndWithdrawnAtNull(borrowerId, type)
            .isPresent();
    }
    
    public List<ConsentResponseDTO> getCurrentConsents(UUID borrowerId) {
        return consentRepository.findAllByBorrowerId(borrowerId)
            .stream()
            .filter(c -> c.getWithdrawnAt() == null)
            .map(this::toConsentResponse)
            .collect(Collectors.toList());
    }
    
    private ConsentResponseDTO toConsentResponse(GDPRConsent consent) {
        return ConsentResponseDTO.builder()
            .consentType(consent.getConsentType())
            .consentedAt(consent.getConsentedAt())
            .withdrawnAt(consent.getWithdrawnAt())
            .version(consent.getVersion())
            .currentlyConsented(consent.getWithdrawnAt() == null)
            .build();
    }
}