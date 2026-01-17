package com.creditapp.bank.model;

/**
 * Enumeration of offer statuses in the lifecycle.
 */
public enum OfferStatus {
    /** Offer calculated by system but not yet submitted to borrower */
    CALCULATED,
    /** Offer submitted to borrower for review */
    SUBMITTED,
    /** Borrower accepted the offer */
    ACCEPTED,
    /** Borrower rejected the offer */
    REJECTED,
    /** Offer expired (validity period passed) */
    EXPIRED,
    /** Bank withdrew the offer */
    WITHDRAWN
}