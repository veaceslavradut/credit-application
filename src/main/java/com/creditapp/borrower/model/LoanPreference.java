package com.creditapp.borrower.model;

import com.creditapp.shared.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_preferences", indexes = {
        @Index(name = "idx_loan_preferences_user_id", columnList = "user_id, created_at"),
        @Index(name = "idx_loan_preferences_purpose", columnList = "user_id, purpose_category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPreference implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "preferred_amount", precision = 15, scale = 2)
    private BigDecimal preferredAmount;

    @Column(name = "min_term")
    private Integer minTerm;

    @Column(name = "max_term")
    private Integer maxTerm;

    @Column(name = "purpose_category", length = 50)
    private String purposeCategory;

    @Column(name = "priority")
    private Integer priority;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}