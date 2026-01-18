package com.creditapp.borrower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferHistoryResponse {
    private List<OfferHistoryRecord> offers;
    private Integer totalCount;
    private Integer limit;
    private Integer offset;
    private Boolean hasMore;
    private LocalDateTime retrievedAt;
}
