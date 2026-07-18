package com.jansetu.backend.scheme.service;

import com.jansetu.backend.scheme.dto.SchemeRequest;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemeService {

    private final SchemeRepository schemeRepository;

    public SchemeService(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    // --- 1. CREATE SCHEME ---
    public SchemeResponse createScheme(SchemeRequest request) {
        Scheme scheme = new Scheme();
        scheme.setName(request.getName());
        scheme.setDescription(request.getDescription());
        scheme.setEligibilityCriteria(request.getEligibilityCriteria());

        Scheme savedScheme = schemeRepository.save(scheme);
        return mapToResponse(savedScheme);
    }

    // --- 2. GET ALL SCHEMES ---
    public List<SchemeResponse> getAllSchemes() {
        return schemeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    // Add this method to SchemeService.java
    public List<Scheme> getAllSchemesEntities() {
        return schemeRepository.findAll();
    }

    // --- HELPER: Map Entity to DTO ---
    private SchemeResponse mapToResponse(Scheme scheme) {
        return SchemeResponse.builder()
                .id(scheme.getId())
                .name(scheme.getName())
                .description(scheme.getDescription())
                .eligibilityCriteria(scheme.getEligibilityCriteria())
                .build();
    }
}