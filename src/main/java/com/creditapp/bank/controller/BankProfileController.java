package com.creditapp.bank.controller;

import com.creditapp.bank.dto.BankProfileDTO;
import com.creditapp.bank.dto.UpdateBankProfileRequest;
import com.creditapp.bank.service.BankProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST controller for bank profile management.
 * All endpoints require bank-level authorization.
 */
@RestController
@RequestMapping("/api/bank/profile")
@Slf4j
@PreAuthorize("hasRole('''BANK_ADMIN''')")
public class BankProfileController {
    private final BankProfileService bankProfileService;

    public BankProfileController(BankProfileService bankProfileService) {
        this.bankProfileService = bankProfileService;
    }

    /**
     * GET /api/bank/profile - Retrieve current bank profile.
     * Response time: <200ms
     */
    @GetMapping
    public ResponseEntity<BankProfileDTO> getBankProfile(Authentication authentication) {
        long startTime = System.currentTimeMillis();
        log.debug("Getting bank profile for user: {}", authentication.getName());

        UUID bankId = extractBankId(authentication);
        BankProfileDTO profile = bankProfileService.getBankProfile(bankId);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Retrieved bank profile in {}ms", duration);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/bank/profile - Update bank profile.
     * Response time: <200ms
     */
    @PutMapping
    public ResponseEntity<BankProfileDTO> updateBankProfile(
            @Valid @RequestBody UpdateBankProfileRequest request,
            Authentication authentication) {
        long startTime = System.currentTimeMillis();
        log.debug("Updating bank profile for user: {}", authentication.getName());

        UUID bankId = extractBankId(authentication);
        BankProfileDTO updated = bankProfileService.updateBankProfile(bankId, request);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Updated bank profile in {}ms", duration);
        return ResponseEntity.ok(updated);
    }

    /**
     * Extract bank ID from authentication context.
     */
    private UUID extractBankId(Authentication authentication) {
        // Implementation depends on how bank ID is stored in authentication
        // This is a placeholder; adjust based on your security implementation
        Object principal = authentication.getPrincipal();
        // For now, assume it can be extracted from a custom principal or claims
        throw new RuntimeException("Bank ID extraction not yet implemented - requires custom security context");
    }
}
