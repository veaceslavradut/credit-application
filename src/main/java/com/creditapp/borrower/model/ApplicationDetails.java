package com.creditapp.borrower.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ApplicationDetails entity for optional application information.
 * One-to-one relationship with Application entity.
 */
@Entity
@Table(name = "application_details", indexes = {
        @Index(name = "idx_application_details_application_id", columnList = "application_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetails {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;

    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(name = "down_payment_amount")
    private BigDecimal downPaymentAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationDetails that = (ApplicationDetails) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
