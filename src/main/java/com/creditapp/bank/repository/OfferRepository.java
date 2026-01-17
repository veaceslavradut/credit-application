package com.creditapp.bank.repository;

import com.creditapp.bank.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByApplicationId(UUID applicationId);
    Optional<Offer> findByApplicationIdAndBankId(UUID applicationId, UUID bankId);
    List<Offer> findByExpiresAtBefore(LocalDateTime expirationTime);
    List<Offer> findByApplicationIdOrderByAprAsc(UUID applicationId);
}