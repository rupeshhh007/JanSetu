package com.jansetu.backend.scheme.dto;

import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SchemeResponse {
    private UUID id;
    private String name;
    private String description;
    private EligibilityCriteria eligibilityCriteria;
}