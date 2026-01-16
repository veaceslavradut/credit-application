package com.creditapp.borrower.model;

/**
 * Document types that can be uploaded to support loan applications.
 */
public enum DocumentType {
    /**
     * Income statement or pay stub
     */
    INCOME_STATEMENT,

    /**
     * Employment verification letter
     */
    EMPLOYMENT_VERIFICATION,

    /**
     * Government-issued identification (passport, driver's license, ID card)
     */
    IDENTIFICATION,

    /**
     * Bank account statements
     */
    BANK_STATEMENT,

    /**
     * Other supporting documents
     */
    OTHER
}
