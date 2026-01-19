package com.creditapp.shared.service;

import com.creditapp.shared.model.Document;
import com.creditapp.shared.model.DocumentType;
import com.creditapp.shared.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    
    @Transactional(readOnly = true)
    public List<Document> findByApplicationId(UUID applicationId) {
        log.debug("Fetching documents for application {}", applicationId);
        return documentRepository.findByApplicationId(applicationId);
    }
    
    @Transactional(readOnly = true)
    public List<Document> findByApplicationIdAndType(UUID applicationId, DocumentType type) {
        log.debug("Fetching {} documents for application {}", type, applicationId);
        return documentRepository.findByApplicationIdAndDocumentType(applicationId, type);
    }
    
    @Transactional(readOnly = true)
    public Optional<Document> findById(UUID id) {
        return documentRepository.findById(id);
    }
    
    @Transactional
    public Document save(Document document) {
        log.debug("Saving document: {}", document.getId());
        return documentRepository.save(document);
    }
    
    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting document: {}", id);
        documentRepository.deleteById(id);
    }
}