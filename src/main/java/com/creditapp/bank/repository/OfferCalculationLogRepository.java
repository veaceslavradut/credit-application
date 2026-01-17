package com.creditapp.bank.repository;

import com.creditapp.bank.model.OfferCalculationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferCalculationLogRepository extends JpaRepository<OfferCalculationLog, Long> {
    List<OfferCalculationLog> findByApplicationId(UUID applicationId);
    List<OfferCalculationLog> findByApplicationIdAndBankId(UUID applicationId, UUID bankId);
}