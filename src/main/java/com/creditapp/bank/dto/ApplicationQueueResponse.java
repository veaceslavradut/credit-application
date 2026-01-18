package com.creditapp.bank.dto;

import com.creditapp.shared.model.ApplicationQueueMetrics;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationQueueResponse {
    private List<ApplicationQueueItem> applications;
    private int totalCount;
    private int limit;
    private int offset;
    private boolean hasMore;

    private ApplicationQueueMetrics queueMetrics;
    private LocalDateTime retrievedAt;
}
