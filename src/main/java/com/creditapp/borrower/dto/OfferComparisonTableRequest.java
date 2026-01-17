package com.creditapp.borrower.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class OfferComparisonTableRequest {
    @Pattern(regexp = "apr|monthlypayment|totalcost|bankname", message = "sortBy must be one of: apr, monthlypayment, totalcost, bankname")
    private String sortBy = "apr";
    
    @Pattern(regexp = "asc|desc", message = "sortOrder must be either asc or desc")
    private String sortOrder = "asc";
    
    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 100, message = "limit cannot exceed 100")
    private Integer limit = 20;
    
    @Min(value = 0, message = "offset must be 0 or greater")
    private Integer offset = 0;
    
    private BigDecimal aprMin;
    private BigDecimal aprMax;
    private BigDecimal monthlyPaymentMin;
    private BigDecimal monthlyPaymentMax;
    
    @Pattern(regexp = "all|national|regional|credit-union", message = "bankCategory must be one of: all, national, regional, credit-union")
    private String bankCategory = "all";
    
    @Pattern(regexp = "full|summary", message = "comparisonMode must be either full or summary")
    private String comparisonMode = "full";

    // Constructors
    public OfferComparisonTableRequest() {}

    // Getters and Setters
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public BigDecimal getAprMin() { return aprMin; }
    public void setAprMin(BigDecimal aprMin) { this.aprMin = aprMin; }

    public BigDecimal getAprMax() { return aprMax; }
    public void setAprMax(BigDecimal aprMax) { this.aprMax = aprMax; }

    public BigDecimal getMonthlyPaymentMin() { return monthlyPaymentMin; }
    public void setMonthlyPaymentMin(BigDecimal monthlyPaymentMin) { this.monthlyPaymentMin = monthlyPaymentMin; }

    public BigDecimal getMonthlyPaymentMax() { return monthlyPaymentMax; }
    public void setMonthlyPaymentMax(BigDecimal monthlyPaymentMax) { this.monthlyPaymentMax = monthlyPaymentMax; }

    public String getBankCategory() { return bankCategory; }
    public void setBankCategory(String bankCategory) { this.bankCategory = bankCategory; }

    public String getComparisonMode() { return comparisonMode; }
    public void setComparisonMode(String comparisonMode) { this.comparisonMode = comparisonMode; }
}
