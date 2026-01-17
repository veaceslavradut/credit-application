package com.creditapp.shared.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CalculationUtils {
    
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final int SCALE = 2;
    
    public static BigDecimal calculateMonthlyPayment(BigDecimal principal, int months, BigDecimal apr) {
        if (principal == null || apr == null || months <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal monthlyRate = apr.divide(HUNDRED, 10, RoundingMode.HALF_UP)
            .divide(TWELVE, 10, RoundingMode.HALF_UP);
        
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), SCALE, RoundingMode.HALF_UP);
        }
        
        BigDecimal one = BigDecimal.ONE;
        BigDecimal numerator = monthlyRate.multiply(
            one.add(monthlyRate).pow(months)
        );
        BigDecimal denominator = one.add(monthlyRate).pow(months).subtract(one);
        
        return principal.multiply(numerator)
            .divide(denominator, SCALE, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateOriginationFee(BigDecimal loanAmount, BigDecimal feePercent) {
        if (loanAmount == null || feePercent == null) {
            return BigDecimal.ZERO;
        }
        return loanAmount.multiply(feePercent)
            .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateInsuranceCost(BigDecimal loanAmount, int months, BigDecimal insurancePercent) {
        if (loanAmount == null || insurancePercent == null || months <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal yearFraction = BigDecimal.valueOf(months).divide(TWELVE, 10, RoundingMode.HALF_UP);
        return loanAmount.multiply(insurancePercent)
            .divide(HUNDRED, 10, RoundingMode.HALF_UP)
            .multiply(yearFraction)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateTotalCost(BigDecimal monthlyPayment, int months, BigDecimal principal) {
        if (monthlyPayment == null || principal == null || months <= 0) {
            return BigDecimal.ZERO;
        }
        return monthlyPayment.multiply(BigDecimal.valueOf(months))
            .subtract(principal)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }
}