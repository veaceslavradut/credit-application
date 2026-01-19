package com.creditapp.borrower.controller;

import com.creditapp.borrower.dto.DeletionRequestResponse;
import com.creditapp.shared.service.DataDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/borrower/data-deletion")
@RequiredArgsConstructor
@Slf4j
public class BorrowerDataDeletionController {
    
    private final DataDeletionService dataDeletionService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<DeletionRequestResponse> requestDeletion(
            @RequestParam(value = "reason", required = false) String reason,
            Authentication authentication) {
        log.info("Initiating data deletion request");
        
        UUID borrowerId = UUID.fromString(authentication.getName());
        DeletionRequestResponse response = dataDeletionService.requestDeletion(borrowerId, reason);
        
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
    
    @PostMapping("/confirm")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<DeletionRequestResponse> confirmDeletion(
            @RequestParam("token") String confirmationToken,
            Authentication authentication) {
        log.info("Confirming data deletion");
        
        UUID borrowerId = UUID.fromString(authentication.getName());
        
        try {
            DeletionRequestResponse response = dataDeletionService.confirmDeletion(confirmationToken, borrowerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error confirming deletion: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("expired")) {
                return new ResponseEntity<>(
                    DeletionRequestResponse.builder()
                        .message("Confirmation token has expired. Please request deletion again.")
                        .status("ERROR")
                        .build(),
                    HttpStatus.BAD_REQUEST
                );
            }
            return new ResponseEntity<>(
                DeletionRequestResponse.builder()
                    .message("Invalid confirmation token.")
                    .status("ERROR")
                    .build(),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<DeletionRequestResponse> cancelDeletion(
            Authentication authentication) {
        log.info("Cancelling data deletion request");
        
        UUID borrowerId = UUID.fromString(authentication.getName());
        
        try {
            DeletionRequestResponse response = dataDeletionService.cancelDeletion(borrowerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error cancelling deletion: {}", e.getMessage());
            return new ResponseEntity<>(
                DeletionRequestResponse.builder()
                    .message(e.getMessage())
                    .status("ERROR")
                    .build(),
                HttpStatus.BAD_REQUEST
            );
        }
    }
}