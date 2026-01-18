package com.creditapp.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationQueueMetrics {
    private int totalApplications;
    private int documentsAwaitingReview;
    private int approvedCount;
    private int rejectedCount;
}
