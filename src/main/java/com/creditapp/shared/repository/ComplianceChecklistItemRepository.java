package com.creditapp.shared.repository;

import com.creditapp.shared.model.ComplianceChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ComplianceChecklistItemRepository extends JpaRepository<ComplianceChecklistItem, UUID> {
    ComplianceChecklistItem findByItemName(String itemName);
}