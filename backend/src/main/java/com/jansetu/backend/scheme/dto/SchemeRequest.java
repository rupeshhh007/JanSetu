package com.jansetu.backend.scheme.dto;

import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchemeRequest {

    @NotBlank(message = "Scheme name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Eligibility criteria are required")
    @Valid // This is crucial for nested validation
    private EligibilityCriteria eligibilityCriteria;
}