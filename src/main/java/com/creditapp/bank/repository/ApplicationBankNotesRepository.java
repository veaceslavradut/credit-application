package com.creditapp.bank.repository;

import com.creditapp.bank.model.ApplicationBankNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ApplicationBankNotes entity
 */
@Repository
public interface ApplicationBankNotesRepository extends JpaRepository<ApplicationBankNotes, Long> {

    /**
     * Find notes for a specific application and bank
     */
    Optional<ApplicationBankNotes> findByApplicationIdAndBankId(UUID applicationId, UUID bankId);

    /**
     * Delete all notes for an application (cleanup on app deletion)
     */
    void deleteByApplicationId(UUID applicationId);
}
