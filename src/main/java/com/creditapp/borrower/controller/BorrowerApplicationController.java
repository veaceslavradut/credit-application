package com.creditapp.borrower.controller;

import com.creditapp.shared.security.RequiresBorrowerRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/borrower")
public class BorrowerApplicationController {

    @RequiresBorrowerRole
    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        Map<String, Object> response = new HashMap<>();
        response.put("applications", Collections.emptyList());
        response.put("message", "Placeholder endpoint - Epic 2 implementation pending");
        return ResponseEntity.ok(response);
    }

    @RequiresBorrowerRole
    @PostMapping("/applications")
    public ResponseEntity<?> createApplication() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Placeholder endpoint - Epic 2 implementation pending");
        return ResponseEntity.status(201).body(response);
    }

    @RequiresBorrowerRole
    @GetMapping("/applications/{id}")
    public ResponseEntity<?> getApplication(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Placeholder endpoint - Epic 2 implementation pending");
        return ResponseEntity.ok(response);
    }

    @RequiresBorrowerRole
    @PutMapping("/applications/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Placeholder endpoint - Epic 2 implementation pending");
        return ResponseEntity.ok(response);
    }
}