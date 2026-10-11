package com.jansetu.backend.scheme.dto;

import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.Instant;
import java.util.List;
import com.jansetu.backend.scheme.entity.SchemeStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeResponse {
    private UUID id;
    private String name;
    private String description;
    private String state;
    private String department;
    private String officialUrl;
    private String sourceName;
    private Instant lastVerifiedAt;
    private List<String> benefits;
    private List<String> requiredDocuments;
    private SchemeStatus status;
    private Instant publishedAt;
    private Instant archivedAt;
    private Instant updatedAt;
    private EligibilityCriteria eligibilityCriteria;
}
