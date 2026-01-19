package com.creditapp.borrower.dto;

import com.creditapp.shared.model.ExportFormat;
import com.creditapp.shared.model.ExportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportResponse {
    private UUID exportId;
    private ExportStatus status;
    private ExportFormat format;
    private String message;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
    private String downloadUrl;
}