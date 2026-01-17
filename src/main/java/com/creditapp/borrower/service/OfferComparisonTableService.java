package com.creditapp.borrower.service;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.dto.OfferComparisonTableRequest;
import com.creditapp.borrower.dto.OfferComparisonTableResponse;
import com.creditapp.borrower.dto.OfferComparisonTableRow;
import com.creditapp.borrower.exception.ApplicationNotFoundException;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.util.PaginationUtils;
import com.creditapp.shared.util.SortBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferComparisonTableService {
    
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;
    
    public OfferComparisonTableResponse getOffersTable(
            UUID applicationId, 
            UUID borrowerId, 
            OfferComparisonTableRequest request) {
        
        log.info("Getting offers table for applicationId={}, borrowerId={}", applicationId, borrowerId);
        
        // Verify borrower owns the application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));
        
        if (!application.getBorrowerId().equals(borrowerId)) {
            log.warn("Access denied: borrower {} attempted to access application {} owned by {}", 
                    borrowerId, applicationId, application.getBorrowerId());
            throw new AccessDeniedException("You do not have permission to access this application");
        }
        
        // Build sort
        var sort = SortBuilder.buildSort(request.getSortBy(), request.getSortOrder());
        
        // Build pageable
        Pageable pageable = PaginationUtils.validateAndGetPageable(request.getLimit(), request.getOffset(), sort);
        
        // Fetch offers with pagination
        Page<Offer> offersPage = offerRepository.findByApplicationId(applicationId, pageable);
        
        // Apply filters
        List<Offer> filteredOffers = applyFilters(offersPage.getContent(), request);
        
        // Get bank details (batch fetch to avoid N+1)
        Set<UUID> bankIds = filteredOffers.stream()
                .map(Offer::getBankId)
                .collect(Collectors.toSet());
        Map<UUID, Organization> bankMap = organizationRepository.findAllById(bankIds)
                .stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));
        
        // Build response rows
        List<OfferComparisonTableRow> rows = filteredOffers.stream()
                .map(offer -> buildRow(offer, bankMap.get(offer.getBankId()), application, request.getComparisonMode()))
                .collect(Collectors.toList());
        
        // Build response
        OfferComparisonTableResponse response = new OfferComparisonTableResponse();
        response.setOffers(rows);
        response.setTotalCount((int) offersPage.getTotalElements());
        response.setLimit(request.getLimit());
        response.setOffset(request.getOffset());
        response.setHasMore(offersPage.getTotalElements() > (request.getOffset() + rows.size()));
        response.setSortBy(request.getSortBy());
        response.setSortOrder(request.getSortOrder());
        response.setAppliedFilters(buildAppliedFilters(request));
        response.setRetrievedAt(LocalDateTime.now());
        
        log.info("Retrieved {} offers for application {}", rows.size(), applicationId);
        return response;
    }
    
    private List<Offer> applyFilters(List<Offer> offers, OfferComparisonTableRequest request) {
        return offers.stream()
                .filter(offer -> applyAprFilter(offer, request.getAprMin(), request.getAprMax()))
                .filter(offer -> applyMonthlyPaymentFilter(offer, request.getMonthlyPaymentMin(), request.getMonthlyPaymentMax()))
                .collect(Collectors.toList());
    }
    
    private boolean applyAprFilter(Offer offer, BigDecimal min, BigDecimal max) {
        if (min != null && offer.getApr().compareTo(min) < 0) {
            return false;
        }
        if (max != null && offer.getApr().compareTo(max) > 0) {
            return false;
        }
        return true;
    }
    
    private boolean applyMonthlyPaymentFilter(Offer offer, BigDecimal min, BigDecimal max) {
        if (min != null && offer.getMonthlyPayment().compareTo(min) < 0) {
            return false;
        }
        if (max != null && offer.getMonthlyPayment().compareTo(max) > 0) {
            return false;
        }
        return true;
    }
    
    private OfferComparisonTableRow buildRow(Offer offer, Organization bank, Application application, String comparisonMode) {
        OfferComparisonTableRow row = new OfferComparisonTableRow();
        row.setOfferId(offer.getId());
        row.setBankId(offer.getBankId());
        row.setBankName(bank != null ? bank.getName() : "Unknown Bank");
        row.setBankLogoUrl(bank != null ? bank.getLogoUrl() : null);
        row.setApr(offer.getApr());
        row.setMonthlyPayment(offer.getMonthlyPayment());
        row.setTotalCost(offer.getTotalCost());
        row.setOfferStatus(offer.getOfferStatus().name());
        row.setSelectButtonState(determineSelectButtonState(offer, application));
        row.setExpirationCountdown(calculateExpirationCountdown(offer.getExpiresAt()));
        row.setExpiresAt(offer.getExpiresAt());
        
        // Include additional fields for full mode
        if ("full".equalsIgnoreCase(comparisonMode)) {
            row.setOriginationFee(offer.getOriginationFee());
            row.setInsuranceCost(offer.getInsuranceCost());
            row.setTermMonths(application.getLoanTermMonths());
            row.setProcessingTimeDays(offer.getProcessingTimeDays());
            row.setValidityPeriodDays(offer.getValidityPeriodDays());
        }
        
        return row;
    }
    
    // Package-private for testing
    String calculateExpirationCountdown(LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, expiresAt);
        
        if (duration.isNegative()) {
            return "Expired";
        }
        
        long totalMinutes = duration.toMinutes();
        
        if (totalMinutes < 1) {
            return "Less than 1 minute";
        }
        
        if (totalMinutes < 60) {
            return totalMinutes + " minute" + (totalMinutes == 1 ? "" : "s");
        }
        
        long hours = duration.toHours();
        if (hours < 24) {
            long minutes = totalMinutes % 60;
            return hours + " hour" + (hours == 1 ? "" : "s") + 
                   (minutes > 0 ? " " + minutes + " minute" + (minutes == 1 ? "" : "s") : "");
        }
        
        long days = duration.toDays();
        long remainingHours = hours % 24;
        return days + " day" + (days == 1 ? "" : "s") + 
               (remainingHours > 0 ? " " + remainingHours + " hour" + (remainingHours == 1 ? "" : "s") : "");
    }
    
    // Package-private for testing
    String determineSelectButtonState(Offer offer, Application application) {
        // Check if offer is expired
        if (offer.getOfferStatus() == OfferStatus.EXPIRED) {
            return "disabled-expired";
        }
        
        // Check if offer is already accepted
        if (offer.getOfferStatus() == OfferStatus.ACCEPTED) {
            return "disabled-selected";
        }
        
        // Check if application is not in the right status
        if (application.getStatus() != ApplicationStatus.SUBMITTED && 
            application.getStatus() != ApplicationStatus.UNDER_REVIEW &&
            application.getStatus() != ApplicationStatus.OFFERS_AVAILABLE) {
            return "disabled-not-ready";
        }
        
        // Offer can be selected
        return "enabled";
    }
    
    private Map<String, Object> buildAppliedFilters(OfferComparisonTableRequest request) {
        Map<String, Object> filters = new HashMap<>();
        
        if (request.getAprMin() != null) {
            filters.put("aprMin", request.getAprMin());
        }
        if (request.getAprMax() != null) {
            filters.put("aprMax", request.getAprMax());
        }
        if (request.getMonthlyPaymentMin() != null) {
            filters.put("monthlyPaymentMin", request.getMonthlyPaymentMin());
        }
        if (request.getMonthlyPaymentMax() != null) {
            filters.put("monthlyPaymentMax", request.getMonthlyPaymentMax());
        }
        if (!"all".equalsIgnoreCase(request.getBankCategory())) {
            filters.put("bankCategory", request.getBankCategory());
        }
        
        return filters;
    }
}
