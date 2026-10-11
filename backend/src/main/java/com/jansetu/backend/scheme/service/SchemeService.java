package com.jansetu.backend.scheme.service;

import com.jansetu.backend.scheme.dto.SchemeRequest;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.exception.SchemePublicationException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.time.Instant;
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
        copyEditableFields(scheme, request);
        scheme.setStatus(SchemeStatus.DRAFT);
        scheme.setPublishedAt(null);

        Scheme savedScheme = schemeRepository.save(scheme);
        return mapToResponse(savedScheme);
    }

    // --- 2. GET ALL SCHEMES ---
    public List<SchemeResponse> getAllSchemes() {
        return schemeRepository.findByStatusOrderByPublishedAtDesc(SchemeStatus.PUBLISHED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SchemeResponse> getAdminSchemes() {
        return schemeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SchemeResponse updateScheme(java.util.UUID id, SchemeRequest request) {
        Scheme scheme = getScheme(id);
        copyEditableFields(scheme, request);
        if (scheme.getStatus() == SchemeStatus.PUBLISHED) validatePublication(scheme);
        return mapToResponse(schemeRepository.save(scheme));
    }

    public SchemeResponse publishScheme(java.util.UUID id) {
        Scheme scheme = getScheme(id);
        validatePublication(scheme);
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setPublishedAt(Instant.now());
        scheme.setArchivedAt(null);
        return mapToResponse(schemeRepository.save(scheme));
    }

    /** Retain catalogue history and application references instead of hard-deleting records. */
    public SchemeResponse archiveScheme(java.util.UUID id) {
        Scheme scheme = getScheme(id);
        scheme.setStatus(SchemeStatus.ARCHIVED);
        scheme.setArchivedAt(Instant.now());
        return mapToResponse(schemeRepository.save(scheme));
    }
    // Add this method to SchemeService.java
    public List<Scheme> getAllSchemesEntities() {
        return schemeRepository.findByStatusOrderByPublishedAtDesc(SchemeStatus.PUBLISHED);
    }

    private void validatePublication(Scheme scheme) {
        if (scheme.getOfficialUrl() == null || scheme.getSourceName() == null || scheme.getLastVerifiedAt() == null) {
            throw new SchemePublicationException(
                    "Official URL, source name, and verification date are required before publishing"
            );
        }
    }

    private Scheme getScheme(java.util.UUID id) {
        return schemeRepository.findById(id)
                .orElseThrow(() -> new SchemeNotFoundException("Scheme not found"));
    }

    private void copyEditableFields(Scheme scheme, SchemeRequest request) {
        scheme.setName(request.getName().trim());
        scheme.setDescription(request.getDescription().trim());
        scheme.setState(request.getState().trim());
        scheme.setDepartment(request.getDepartment().trim());
        scheme.setOfficialUrl(blankToNull(request.getOfficialUrl()));
        scheme.setSourceName(blankToNull(request.getSourceName()));
        scheme.setLastVerifiedAt(request.getLastVerifiedAt());
        scheme.setBenefits(request.getBenefits() == null ? Collections.emptyList() : List.copyOf(request.getBenefits()));
        scheme.setRequiredDocuments(request.getRequiredDocuments() == null ? Collections.emptyList() : List.copyOf(request.getRequiredDocuments()));
        scheme.setEligibilityCriteria(request.getEligibilityCriteria());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    // --- HELPER: Map Entity to DTO ---
    private SchemeResponse mapToResponse(Scheme scheme) {
        return SchemeResponse.builder()
                .id(scheme.getId())
                .name(scheme.getName())
                .description(scheme.getDescription())
                .state(scheme.getState())
                .department(scheme.getDepartment())
                .officialUrl(scheme.getOfficialUrl())
                .sourceName(scheme.getSourceName())
                .lastVerifiedAt(scheme.getLastVerifiedAt())
                .benefits(scheme.getBenefits())
                .requiredDocuments(scheme.getRequiredDocuments())
                .status(scheme.getStatus())
                .publishedAt(scheme.getPublishedAt())
                .archivedAt(scheme.getArchivedAt())
                .updatedAt(scheme.getUpdatedAt())
                .eligibilityCriteria(scheme.getEligibilityCriteria())
                .build();
    }
}
