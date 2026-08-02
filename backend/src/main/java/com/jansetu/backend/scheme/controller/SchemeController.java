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

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    // --- 1. CREATE SCHEME ---
    @PostMapping
    public ResponseEntity<SchemeResponse> createScheme(@Valid @RequestBody SchemeRequest request) {
        SchemeResponse response = schemeService.createScheme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheme(@PathVariable java.util.UUID id) {
        schemeService.deleteScheme(id);
        return ResponseEntity.noContent().build();
    }

    // --- 2. GET ALL SCHEMES ---
    @GetMapping
    public ResponseEntity<List<SchemeResponse>> getAllSchemes() {
        return ResponseEntity.ok(schemeService.getAllSchemes());
    }
}