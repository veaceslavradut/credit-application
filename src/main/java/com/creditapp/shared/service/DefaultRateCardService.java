package com.creditapp.shared.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.borrower.model.LoanType;
import com.creditapp.borrower.model.Currency;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultRateCardService {
    
    private final BigDecimal defaultApr;
    private final BigDecimal defaultOriginationFee;
    private final BigDecimal defaultInsuranceCost;
    
    public DefaultRateCardService() {
        this.defaultApr = BigDecimal.valueOf(8.5);
        this.defaultOriginationFee = BigDecimal.valueOf(1.5);
        this.defaultInsuranceCost = BigDecimal.valueOf(0.5);
        log.info("DefaultRateCardService initialized with APR: {}, Origination Fee: {}, Insurance: {}", 
            defaultApr, defaultOriginationFee, defaultInsuranceCost);
    }
    
    public BankRateCard getDefaultRateCard() {
        BankRateCard defaultCard = new BankRateCard();
        defaultCard.setId(null);
        defaultCard.setBankId(null);
        defaultCard.setLoanType(LoanType.PERSONAL);
        defaultCard.setCurrency(Currency.EUR);
        defaultCard.setMinLoanAmount(BigDecimal.valueOf(1000));
        defaultCard.setMaxLoanAmount(BigDecimal.valueOf(5000000));
        defaultCard.setBaseApr(defaultApr);
        defaultCard.setAprAdjustmentRange(BigDecimal.ZERO);
        defaultCard.setOriginationFeePercent(defaultOriginationFee);
        defaultCard.setInsurancePercent(defaultInsuranceCost);
        return defaultCard;
    }
}