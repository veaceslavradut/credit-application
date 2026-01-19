package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for offer history filtering and sorting options.
 * Used to pass filter criteria to the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferHistoryFilter {

    private List<String> statuses; // SUBMITTED, ACCEPTED, EXPIRED, WITHDRAWN
    
    private LocalDate dateFrom;
    
    private LocalDate dateTo;
    
    private BigDecimal aprFrom;
    
    private BigDecimal aprTo;
    
    private BigDecimal paymentFrom;
    
    private BigDecimal paymentTo;
    
    private String sortBy; // submittedDate, apr, monthlyPayment with ASC/DESC suffix
}
