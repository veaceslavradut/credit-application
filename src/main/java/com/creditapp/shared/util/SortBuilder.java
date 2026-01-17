package com.creditapp.shared.util;

import org.springframework.data.domain.Sort;

public class SortBuilder {
    
    public static Sort buildSort(String sortBy, String sortOrder) {
        String order = (sortOrder != null && "desc".equalsIgnoreCase(sortOrder)) ? "desc" : "asc";
        Sort.Direction direction = "desc".equals(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        String field = "apr";
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "monthlypayment": field = "monthlyPayment"; break;
                case "totalcost": field = "totalCost"; break;
                case "bankname": field = "bankName"; break;
                default: field = "apr";
            }
        }
        
        return Sort.by(new Sort.Order(direction, field));
    }
}
