package com.creditapp.bank.service;

import com.creditapp.bank.dto.*;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationDetails;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for retrieving complete application details for bank review panel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankApplicationDetailsService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    /**
     * Get full application details for bank review panel
     * Includes borrower info, loan details, employment, consents, and offer
     *
     * @param bankId bank admin's organization id
     * @param applicationId the application to retrieve
     * @return complete application details response
     */
    @Cacheable(value = "applicationDetails", key = "#bankId + '-' + #applicationId", unless = "#result == null")
    public ApplicationDetailsResponse getApplicationDetails(UUID bankId, UUID applicationId) {
        long startTime = System.currentTimeMillis();
        
        // Fetch application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Verify bank has an offer for this application
        Offer bankOffer = offerRepository.findByApplicationIdAndBankId(applicationId, bankId)
                .orElseThrow(() -> new IllegalArgumentException("Bank has no offer for this application"));

        // Fetch borrower
        User borrower = userRepository.findById(application.getBorrowerId())
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found"));

        // Build borrower details
        BorrowerDetailsDTO borrowerDetails = BorrowerDetailsDTO.builder()
                .firstName(borrower.getFirstName())
                .lastName(borrower.getLastName())
                .email(borrower.getEmail())
                .phone(borrower.getPhoneNumber())
                .address("")
                .build();

        // Build loan request details
        LoanRequestDetailsDTO loanRequest = LoanRequestDetailsDTO.builder()
                .loanType(application.getLoanType())
                .amount(application.getLoanAmount())
                .termMonths(application.getLoanTermMonths())
                .purpose("")
                .incomeDocumentationStatus("PENDING")
                .build();

        // Build employment details from ApplicationDetails
        ApplicationDetails appDetails = application.getDetails();
        EmploymentDetailsDTO employment = EmploymentDetailsDTO.builder()
                .employer("")
                .position("")
                .annualIncome(appDetails != null ? appDetails.getAnnualIncome() : null)
                .yearsEmployed(0)
                .build();

        // Build consent details (3 standard consents)
        List<ConsentDetailsDTO> consents = buildConsents();

        // Build offer DTO from bank's offer
        OfferDTO offerDto = buildOfferDTO(bankOffer);

        // Build response
        ApplicationDetailsResponse response = ApplicationDetailsResponse.builder()
                .applicationId(applicationId)
                .borrower(borrowerDetails)
                .loanRequest(loanRequest)
                .employment(employment)
                .consents(consents)
                .offer(offerDto)
                .internalNotes("")
                .build();

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Retrieved application details for app {} from bank {} in {}ms", applicationId, bankId, elapsed);

        if (elapsed > 200) {
            log.warn("Application details retrieval exceeded 200ms SLA: {}ms", elapsed);
        }

        return response;
    }

    private List<ConsentDetailsDTO> buildConsents() {
        List<ConsentDetailsDTO> consents = new ArrayList<>();
        
        // Consent 1: Credit Check
        consents.add(ConsentDetailsDTO.builder()
                .consentNumber(1)
                .consentText("I authorize the bank to check my credit report and history")
                .signed(false)
                .build());

        // Consent 2: Third-party Share
        consents.add(ConsentDetailsDTO.builder()
                .consentNumber(2)
                .consentText("I authorize the bank to share my information with third-party service providers")
                .signed(false)
                .build());

        // Consent 3: Loan Service Terms
        consents.add(ConsentDetailsDTO.builder()
                .consentNumber(3)
                .consentText("I agree to the loan service terms and conditions")
                .signed(false)
                .build());

        return consents;
    }

    private OfferDTO buildOfferDTO(Offer offer) {
        OfferDTO offerDto = new OfferDTO();
        offerDto.id = offer.getId();
        offerDto.applicationId = offer.getApplicationId();
        offerDto.bankId = offer.getBankId();
        offerDto.apr = offer.getApr();
        offerDto.monthlyPayment = offer.getMonthlyPayment();
        offerDto.totalCost = offer.getTotalCost();
        offerDto.originationFee = offer.getOriginationFee();
        offerDto.processingTimeDays = offer.getProcessingTimeDays();
        offerDto.validityPeriodDays = offer.getValidityPeriodDays();
        offerDto.offerStatus = offer.getOfferStatus();
        offerDto.expiresAt = offer.getExpiresAt();
        offerDto.createdAt = offer.getCreatedAt();
        return offerDto;
    }
}
