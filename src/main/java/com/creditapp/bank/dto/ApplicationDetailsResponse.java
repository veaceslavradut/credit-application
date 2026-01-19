package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for full application details in review panel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetailsResponse {
    private UUID applicationId;
    private BorrowerDetailsDTO borrower;
    private LoanRequestDetailsDTO loanRequest;
    private EmploymentDetailsDTO employment;
    private List<ConsentDetailsDTO> consents;
    private OfferDTO offer;
    private String internalNotes;
}
