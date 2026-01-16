package com.creditapp.borrower.model;

/**
 * Application status lifecycle enum.
 * Defines valid states for borrower loan applications.
 */
public enum ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    OFFERS_AVAILABLE,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    WITHDRAWN,
    COMPLETED
}
