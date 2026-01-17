package com.creditapp.shared.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationUtils {
    private static final int MAX_PAGE_SIZE = 100;
    
    public static Pageable validateAndGetPageable(int limit, int offset) {
        if (limit < 1) {
            limit = 20;
        } else if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        
        if (offset < 0) {
            offset = 0;
        }
        
        int pageNumber = offset / limit;
        return PageRequest.of(pageNumber, limit);
    }
}
