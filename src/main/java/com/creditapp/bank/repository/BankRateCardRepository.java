package com.creditapp.bank.repository;

import com.creditapp.bank.model.BankRateCard;
import com.creditapp.borrower.model.LoanType;
import com.creditapp.borrower.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRateCardRepository extends JpaRepository<BankRateCard, UUID> {
    Optional<BankRateCard> findByBankIdAndLoanTypeAndCurrencyAndValidToIsNull(UUID bankId, LoanType loanType, Currency currency);
    List<BankRateCard> findByBankIdAndValidToIsNull(UUID bankId);
    List<BankRateCard> findByValidToIsNull();
}