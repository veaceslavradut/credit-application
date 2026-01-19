package com.creditapp.bank.controller;

import com.creditapp.bank.dto.WithdrawOfferResponse;
import com.creditapp.bank.service.BankOfferWithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bank/offers")
@RequiredArgsConstructor
public class BankOfferWithdrawalController {

    private final BankOfferWithdrawalService withdrawalService;

    @DeleteMapping("/{offerId}/withdraw")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<WithdrawOfferResponse> withdrawOffer(
            @PathVariable UUID offerId) {
        
        log.info("Received withdrawal request for offer {}", offerId);
        
        // Extract bankId from SecurityContext (placeholder - will be replaced with actual security context)
        UUID bankId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        
        WithdrawOfferResponse response = withdrawalService.withdrawOffer(bankId, offerId);
        
        return ResponseEntity.ok(response);
    }
}