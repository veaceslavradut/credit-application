package com.creditapp.shared.repository;

import com.creditapp.shared.model.DataExport;
import com.creditapp.shared.model.ExportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataExportRepository extends JpaRepository<DataExport, UUID> {
    Optional<DataExport> findByDownloadToken(String token);
    
    List<DataExport> findByBorrowerIdAndStatus(UUID borrowerId, ExportStatus status, Pageable pageable);
    
    List<DataExport> findByBorrowerIdOrderByRequestedAtDesc(UUID borrowerId, Pageable pageable);
}