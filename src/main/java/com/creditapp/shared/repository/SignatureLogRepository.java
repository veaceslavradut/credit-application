package com.creditapp.shared.repository;

import com.creditapp.shared.model.SignatureLog;
import com.creditapp.shared.model.SignatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignatureLogRepository extends JpaRepository<SignatureLog, UUID> {
    List<SignatureLog> findByDocumentId(UUID documentId);
    List<SignatureLog> findByDocumentIdAndSignatureStatus(UUID documentId, SignatureStatus status);
}