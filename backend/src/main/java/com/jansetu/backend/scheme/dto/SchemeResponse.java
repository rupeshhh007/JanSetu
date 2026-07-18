package com.jansetu.backend.scheme.dto;

import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeResponse {
    private UUID id;
    private String name;
    private String description;
    private EligibilityCriteria eligibilityCriteria;
}