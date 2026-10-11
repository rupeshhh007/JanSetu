package com.jansetu.backend.scheme.controller;

import com.jansetu.backend.scheme.dto.SchemeRequest;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.service.SchemeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schemes")
public class SchemeController {

    private final SchemeService schemeService;

    private final com.jansetu.backend.scheme.service.SchemeEligibilityService eligibility;
    public SchemeController(SchemeService schemeService,
            com.jansetu.backend.scheme.service.SchemeEligibilityService eligibility) {
        this.schemeService = schemeService;
        this.eligibility = eligibility;
    }

    @GetMapping("/{id}/eligibility")
    public com.jansetu.backend.scheme.evaluator.EligibilityEvaluation checkEligibility(@PathVariable java.util.UUID id) {
        return eligibility.check(id);
    }

    // --- 1. CREATE SCHEME ---
    @PostMapping
    public ResponseEntity<SchemeResponse> createScheme(@Valid @RequestBody SchemeRequest request) {
        SchemeResponse response = schemeService.createScheme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchemeResponse> updateScheme(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody SchemeRequest request
    ) {
        return ResponseEntity.ok(schemeService.updateScheme(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<SchemeResponse> publishScheme(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(schemeService.publishScheme(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SchemeResponse> archiveScheme(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(schemeService.archiveScheme(id));
    }

    // --- 2. GET ALL SCHEMES ---
    @GetMapping
    public ResponseEntity<List<SchemeResponse>> getAllSchemes() {
        return ResponseEntity.ok(schemeService.getAllSchemes());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<SchemeResponse>> getAdminSchemes() {
        return ResponseEntity.ok(schemeService.getAdminSchemes());
    }
}
