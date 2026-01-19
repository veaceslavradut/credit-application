package com.creditapp.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing internal notes about an application at the bank level.
 * These notes are visible only to bank staff, not the borrower.
 */
@Entity
@Table(name = "application_bank_notes", indexes = {
        @Index(name = "idx_app_bank_notes_app_id", columnList = "application_id"),
        @Index(name = "idx_app_bank_notes_bank_id", columnList = "bank_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationBankNotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "bank_id", nullable = false)
    private UUID bankId;

    @Column(name = "notes", columnDefinition = "TEXT", length = 4000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;
}
