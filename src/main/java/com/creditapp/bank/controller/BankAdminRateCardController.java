package com.creditapp.bank.controller;

import com.creditapp.bank.dto.BankRateCardRequest;
import com.creditapp.bank.dto.BankRateCardResponse;
import com.creditapp.bank.service.BankRateCardService;
import com.creditapp.shared.security.AuthorizationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank/rate-cards")
public class BankAdminRateCardController {
    
    private final BankRateCardService bankRateCardService;
    private final AuthorizationService authorizationService;
    
    public BankAdminRateCardController(BankRateCardService bankRateCardService,
            AuthorizationService authorizationService) {
        this.bankRateCardService = bankRateCardService;
        this.authorizationService = authorizationService;
    }
    
    /**
     * Creates a new rate card for the authenticated bank.
     *
     * @param request The rate card creation request
     * @return ResponseEntity with status 201 (Created) and the created rate card
     */
    @PostMapping
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<BankRateCardResponse> createRateCard(
            @Valid @RequestBody BankRateCardRequest request) {
        UUID bankId = authorizationService.getBankIdFromContext();
        BankRateCardResponse response = bankRateCardService.createRateCard(bankId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieves all active rate cards for the authenticated bank.
     *
     * @return ResponseEntity with status 200 (OK) and list of rate cards
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<List<BankRateCardResponse>> getActiveRateCards() {
        UUID bankId = authorizationService.getBankIdFromContext();
        List<BankRateCardResponse> response = bankRateCardService.getActiveRateCards(bankId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates an existing rate card by creating a new version.
     * The old card is marked as inactive (versioning).
     *
     * @param rateCardId The ID of the rate card to update
     * @param request The rate card update request
     * @return ResponseEntity with status 200 (OK) and the new rate card version
     */
    @PutMapping("/{rateCardId}")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<BankRateCardResponse> updateRateCard(
            @PathVariable UUID rateCardId,
            @Valid @RequestBody BankRateCardRequest request) {
        UUID bankId = authorizationService.getBankIdFromContext();
        BankRateCardResponse response = bankRateCardService.updateRateCard(bankId, rateCardId, request);
        return ResponseEntity.ok(response);
    }
}