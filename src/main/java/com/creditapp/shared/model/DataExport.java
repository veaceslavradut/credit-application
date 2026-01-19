package com.creditapp.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_exports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID borrowerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportFormat format;

    @Column(length = 500)
    private String fileUrl;

    @Column(length = 50, unique = true)
    private String downloadToken;

    private LocalDateTime downloadTokenExpiresAt;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    @Column(length = 45)
    private String createdByIp;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ExportStatus.PENDING;
        }
    }
}