package com.creditapp.shared.repository;

import com.creditapp.shared.model.Document;
import com.creditapp.shared.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByApplicationId(UUID applicationId);
    List<Document> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
}