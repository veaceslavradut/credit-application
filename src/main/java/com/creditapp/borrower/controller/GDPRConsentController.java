package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.ConsentRequestDTO;
import com.creditapp.borrower.dto.ConsentResponseDTO;
import com.creditapp.borrower.dto.ConsentWithdrawalRequest;
import com.creditapp.shared.service.GDPRConsentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/borrower/consent")
@RequiredArgsConstructor
@Slf4j
public class GDPRConsentController {
    
    private final GDPRConsentService consentService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<List<ConsentResponseDTO>> getCurrentConsents() {
        UUID borrowerId = extractBorrowerId();
        log.info("Fetching current consents for borrower {}", borrowerId);
        List<ConsentResponseDTO> consents = consentService.getCurrentConsents(borrowerId);
        return ResponseEntity.ok(consents);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<List<ConsentResponseDTO>> grantConsent(
            @Valid @RequestBody ConsentRequestDTO request,
            HttpServletRequest httpRequest) {
        UUID borrowerId = extractBorrowerId();
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        log.info("Granting consent for borrower {} with types {}", borrowerId, request.getConsentTypes());
        List<ConsentResponseDTO> responses = consentService.grantConsent(borrowerId, request.getConsentTypes(), ipAddress, userAgent);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @PutMapping("/withdraw")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ConsentResponseDTO> withdrawConsent(@Valid @RequestBody ConsentWithdrawalRequest request) {
        UUID borrowerId = extractBorrowerId();
        log.info("Withdrawing consent for borrower {} with type {}", borrowerId, request.getConsentType());
        ConsentResponseDTO response = consentService.withdrawConsent(borrowerId, request.getConsentType());
        return ResponseEntity.ok(response);
    }
    
    private UUID extractBorrowerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(authentication.getPrincipal().toString());
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}