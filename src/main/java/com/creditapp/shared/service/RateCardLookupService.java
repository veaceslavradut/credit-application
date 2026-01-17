package com.creditapp.shared.service;

import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.creditapp.bank.model.BankRateCard;
import com.creditapp.bank.repository.BankRateCardRepository;
import com.creditapp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateCardLookupService {
    
    private final BankRateCardRepository bankRateCardRepository;
    private final DefaultRateCardService defaultRateCardService;
    
    @Cacheable(value = "rateCards", key = "#bankId.toString()", unless = "#result == null")
    public BankRateCard getRateCard(UUID bankId) {
        log.debug("Fetching rate card for bank: {}", bankId);
        List<BankRateCard> rateCards = bankRateCardRepository.findByBankIdAndValidToIsNull(bankId);
        if (rateCards.isEmpty()) {
            throw new NotFoundException("Rate card not found for bank: " + bankId);
        }
        return rateCards.get(0);
    }
    
    public BankRateCard getDefaultRateCard() {
        log.debug("Fetching default rate card");
        return defaultRateCardService.getDefaultRateCard();
    }
}