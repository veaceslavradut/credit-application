package com.creditapp.bank.model;

/**
 * Enumeration of calculation types for offer calculation log.
 */
public enum CalculationType {
    /** Calculation based on mock/simulated data */
    MOCK_CALCULATION,
    /** Calculation from real bank API */
    REAL_API,
    /** Calculation manually overridden by admin */
    OVERRIDE
}