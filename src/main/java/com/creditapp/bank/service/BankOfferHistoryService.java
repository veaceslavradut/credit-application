package com.creditapp.bank.service;

import com.creditapp.bank.dto.OfferHistoryFilter;
import com.creditapp.bank.dto.OfferHistoryItem;
import com.creditapp.bank.dto.OfferHistoryResponse;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationStatus;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.User;
import com.creditapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for retrieving bank offer history with filtering, sorting, and pagination
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankOfferHistoryService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    /**
     * Get paginated offer history for a bank with filtering and sorting.
     * Target response time: <200ms
     */
    public OfferHistoryResponse getOfferHistory(
            UUID bankId,
            OfferHistoryFilter filter,
            int page,
            int pageSize) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Fetch all offers for this bank (use Pageable with large size to get all)
            Pageable largePage = PageRequest.of(0, 10000);
            List<Offer> offers = offerRepository.findByBankId(bankId, largePage)
                    .stream()
                    .toList();
            
            // Apply filters
            List<Offer> filtered = applyFilters(offers, filter);
            
            // Sort
            List<Offer> sorted = sortOffers(filtered, filter.getSortBy());
            
            // Convert to DTOs
            List<OfferHistoryItem> items = sorted.stream()
                    .map(this::convertToHistoryItem)
                    .collect(Collectors.toList());
            
            // Apply pagination
            int totalCount = items.size();
            int startIdx = page * pageSize;
            int endIdx = Math.min(startIdx + pageSize, totalCount);
            
            List<OfferHistoryItem> pageItems = items.isEmpty() || startIdx >= totalCount ? 
                    new ArrayList<>() : items.subList(startIdx, endIdx);
            
            long responseTime = System.currentTimeMillis() - startTime;
            if (responseTime > 200) {
                log.warn("Slow offer history query: {}ms for bankId={}", responseTime, bankId);
            }
            
            return new OfferHistoryResponse(pageItems, (long) totalCount, page, pageSize);
            
        } catch (Exception e) {
            log.error("Error retrieving offer history for bankId={}", bankId, e);
            throw new RuntimeException("Failed to retrieve offer history", e);
        }
    }
    
    /**
     * Determine the current status of an offer.
     */
    private String determineOfferStatus(Offer offer) {
        if (offer == null) {
            return "SUBMITTED";
        }
        
        OfferStatus offerStatus = offer.getOfferStatus();
        
        if (offerStatus == OfferStatus.WITHDRAWN) {
            return "WITHDRAWN";
        }
        
        if (offerStatus == OfferStatus.EXPIRED || offerStatus == OfferStatus.EXPIRED_WITH_SELECTION) {
            return "EXPIRED";
        }
        
        if (offerStatus == OfferStatus.ACCEPTED) {
            return "ACCEPTED";
        }
        
        return "SUBMITTED";
    }
    
    /**
     * Determine the borrower's interaction status with the offer.
     */
    private String determineBorrowerStatus(Offer offer) {
        if (offer == null) {
            return "NOT_VIEWED";
        }
        
        // If borrower has selected this offer, return ACCEPTED_OTHER
        if (offer.getBorrowerSelectedAt() != null) {
            return "ACCEPTED_OTHER";
        }
        
        // Try to get the associated application
        try {
            UUID applicationId = offer.getApplicationId();
            if (applicationId != null) {
                Optional<Application> appOpt = applicationRepository.findById(applicationId);
                if (appOpt.isPresent()) {
                    Application app = appOpt.get();
                    ApplicationStatus appStatus = app.getStatus();
                    
                    // If application status indicates borrower has viewed offers
                    if (appStatus == ApplicationStatus.OFFERS_AVAILABLE || 
                        appStatus == ApplicationStatus.ACCEPTED ||
                        appStatus == ApplicationStatus.UNDER_REVIEW) {
                        return "VIEWED";
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not determine borrower status for offerId={}", offer.getId(), e);
        }
        
        return "NOT_VIEWED";
    }
    
    /**
     * Apply all active filters to the offer list.
     */
    private List<Offer> applyFilters(List<Offer> offers, OfferHistoryFilter filter) {
        return offers.stream()
                .filter(offer -> passesStatusFilter(offer, filter.getStatuses()))
                .filter(offer -> passesDateFilter(offer, filter.getDateFrom(), filter.getDateTo()))
                .filter(offer -> passesAprFilter(offer, filter.getAprFrom(), filter.getAprTo()))
                .filter(offer -> passesPaymentFilter(offer, filter.getPaymentFrom(), filter.getPaymentTo()))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if offer passes status filter.
     */
    private boolean passesStatusFilter(Offer offer, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return true;
        }
        
        String offerStatus = determineOfferStatus(offer);
        return statuses.contains(offerStatus);
    }
    
    /**
     * Check if offer passes date range filter.
     */
    private boolean passesDateFilter(Offer offer, LocalDate dateFrom, LocalDate dateTo) {
        if (offer == null || offer.getOfferSubmittedAt() == null) {
            return true;
        }
        
        LocalDate offerDate = offer.getOfferSubmittedAt().toLocalDate();
        
        if (dateFrom != null && offerDate.isBefore(dateFrom)) {
            return false;
        }
        
        if (dateTo != null && offerDate.isAfter(dateTo)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if offer passes APR range filter.
     */
    private boolean passesAprFilter(Offer offer, BigDecimal aprFrom, BigDecimal aprTo) {
        if (offer == null || offer.getApr() == null) {
            return true;
        }
        
        BigDecimal apr = offer.getApr();
        
        if (aprFrom != null && apr.compareTo(aprFrom) < 0) {
            return false;
        }
        
        if (aprTo != null && apr.compareTo(aprTo) > 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if offer passes monthly payment range filter.
     */
    private boolean passesPaymentFilter(Offer offer, BigDecimal paymentFrom, BigDecimal paymentTo) {
        if (offer == null || offer.getMonthlyPayment() == null) {
            return true;
        }
        
        BigDecimal payment = offer.getMonthlyPayment();
        
        if (paymentFrom != null && payment.compareTo(paymentFrom) < 0) {
            return false;
        }
        
        if (paymentTo != null && payment.compareTo(paymentTo) > 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Sort offers based on sort criteria.
     * Format: "fieldName_DIRECTION" (e.g., "offerSubmittedAt_DESC", "apr_ASC")
     */
    private List<Offer> sortOffers(List<Offer> offers, String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "offerSubmittedAt_DESC";
        }
        
        final String finalSortBy = sortBy;
        
        return offers.stream()
                .sorted((o1, o2) -> compareOffers(o1, o2, finalSortBy))
                .collect(Collectors.toList());
    }
    
    /**
     * Compare two offers for sorting.
     */
    private int compareOffers(Offer o1, Offer o2, String sortCriteria) {
        try {
            String[] parts = sortCriteria.split("_");
            String field = parts[0];
            boolean ascending = parts.length > 1 && "ASC".equals(parts[1]);
            
            int result = 0;
            
            if ("offerSubmittedAt".equals(field)) {
                result = o1.getOfferSubmittedAt().compareTo(o2.getOfferSubmittedAt());
            } else if ("apr".equals(field)) {
                result = o1.getApr().compareTo(o2.getApr());
            } else if ("monthlyPayment".equals(field)) {
                result = o1.getMonthlyPayment().compareTo(o2.getMonthlyPayment());
            } else {
                // Default to offer submission date descending
                result = o2.getOfferSubmittedAt().compareTo(o1.getOfferSubmittedAt());
                return result;
            }
            
            return ascending ? result : -result;
        } catch (Exception e) {
            // Fall back to default sort
            return o2.getOfferSubmittedAt().compareTo(o1.getOfferSubmittedAt());
        }
    }
    
    /**
     * Convert Offer entity to OfferHistoryItem DTO.
     */
    private OfferHistoryItem convertToHistoryItem(Offer offer) {
        try {
            String borrowerName = "Unknown";
            
            if (offer.getApplicationId() != null) {
                try {
                    Optional<Application> appOpt = applicationRepository.findById(offer.getApplicationId());
                    if (appOpt.isPresent()) {
                        Application app = appOpt.get();
                        if (app.getBorrowerId() != null) {
                            Optional<User> userOpt = userRepository.findById(app.getBorrowerId());
                            if (userOpt.isPresent()) {
                                User user = userOpt.get();
                                borrowerName = (user.getFirstName() != null ? user.getFirstName() : "") +
                                             " " +
                                             (user.getLastName() != null ? user.getLastName() : "");
                                borrowerName = borrowerName.trim();
                                if (borrowerName.isEmpty()) {
                                    borrowerName = "Unknown";
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not retrieve borrower name for applicationId={}", offer.getApplicationId(), e);
                }
            }
            
            return new OfferHistoryItem(
                    offer.getId(),
                    borrowerName,
                    offer.getApr(),
                    offer.getMonthlyPayment(),
                    offer.getProcessingTimeDays(),
                    offer.getOfferSubmittedAt(),
                    determineOfferStatus(offer),
                    determineBorrowerStatus(offer)
            );
        } catch (Exception e) {
            log.error("Error converting offer {} to history item", offer.getId(), e);
            throw new RuntimeException("Failed to convert offer to history item", e);
        }
    }
}
