package com.creditapp.bank.controller;

import com.creditapp.bank.dto.BankOfferSubmissionRequest;
import com.creditapp.bank.dto.BankOfferSubmissionResponse;
import com.creditapp.bank.service.BankOfferSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bank/offers")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BankOfferSubmissionController {

    private final BankOfferSubmissionService offerSubmissionService;

    @PostMapping("/submit")
    @PreAuthorize("hasAuthority(''BANK_OFFICER'')")
    public ResponseEntity<BankOfferSubmissionResponse> submitOffer(
            @Valid @RequestBody BankOfferSubmissionRequest request,
            @RequestParam UUID bankId,
            @RequestParam UUID officerId) {

        log.info("[BANK_OFFER] Officer {} submitting offer for application {}", 
                officerId, request.getApplicationId());

        BankOfferSubmissionResponse response = offerSubmissionService.submitOffer(
                bankId,
                officerId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}