package com.creditapp.bank.controller;

import com.creditapp.bank.dto.DeclineApplicationRequest;
import com.creditapp.bank.dto.DeclineApplicationResponse;
import com.creditapp.bank.service.BankApplicationDeclineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bank/applications")
@RequiredArgsConstructor
public class BankApplicationDeclineController {

    private final BankApplicationDeclineService declineService;

    @PostMapping("/{applicationId}/decline")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public ResponseEntity<DeclineApplicationResponse> declineApplication(
            @PathVariable UUID applicationId,
            @RequestBody DeclineApplicationRequest request) {
        
        log.info("Received decline request for application {}", applicationId);
        
        // Extract bankId from SecurityContext (placeholder - will be replaced with actual security context)
        UUID bankId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        
        DeclineApplicationResponse response = declineService.declineApplication(bankId, applicationId, request);
        
        return ResponseEntity.ok(response);
    }
}