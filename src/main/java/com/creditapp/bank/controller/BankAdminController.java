package com.creditapp.bank.controller;

import com.creditapp.shared.security.RequiresBankAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankAdminController {

    @RequiresBankAdmin
    @GetMapping("/queue")
    public ResponseEntity<?> getApplicationQueue() {
        Map<String, Object> response = new HashMap<>();
        response.put("applications", Collections.emptyList());
        response.put("message", "Placeholder endpoint - Epic 3/4 implementation pending");
        return ResponseEntity.ok(response);
    }

    @RequiresBankAdmin
    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        Map<String, Object> response = new HashMap<>();
        response.put("applications", Collections.emptyList());
        response.put("message", "Placeholder endpoint - Epic 3/4 implementation pending");
        return ResponseEntity.ok(response);
    }

    @RequiresBankAdmin
    @PostMapping("/offers")
    public ResponseEntity<?> createOffer() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Placeholder endpoint - Epic 3/4 implementation pending");
        return ResponseEntity.status(201).body(response);
    }

    @RequiresBankAdmin
    @GetMapping("/offers/{id}")
    public ResponseEntity<?> getOffer(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Placeholder endpoint - Epic 3/4 implementation pending");
        return ResponseEntity.ok(response);
    }
}