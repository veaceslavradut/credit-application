package com.creditapp.shared.job;

import com.creditapp.shared.model.DeletionRequest;
import com.creditapp.shared.model.DeletionStatus;
import com.creditapp.shared.repository.DeletionRequestRepository;
import com.creditapp.shared.service.DataDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataDeletionJob {
    
    private final DeletionRequestRepository deletionRequestRepository;
    private final DataDeletionService dataDeletionService;
    
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void processPendingDeletions() {
        log.info("Starting scheduled data deletion job");
        try {
            List<DeletionRequest> confirmedRequests = deletionRequestRepository.findByStatusOrderByRequestedAtAsc(DeletionStatus.CONFIRMED);
            log.info("Found {} confirmed deletion requests to process", confirmedRequests.size());
            for (DeletionRequest request : confirmedRequests) {
                try {
                    log.info("Processing deletion request: {}", request.getId());
                    dataDeletionService.executeDataDeletion(request.getId());
                    log.info("Successfully queued deletion for request: {}", request.getId());
                } catch (Exception e) {
                    log.error("Error processing deletion request: {}", request.getId(), e);
                }
            }
            log.info("Scheduled data deletion job completed");
        } catch (Exception e) {
            log.error("Error in scheduled data deletion job", e);
        }
    }
}