package com.creditapp.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated offer history response.
 * Contains list of offers and pagination metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferHistoryResponse {

    private List<OfferHistoryItem> items;
    
    private Long totalCount;
    
    private Integer page;
    
    private Integer pageSize;
}
