package com.jansetu.backend.scheme.dto;

import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class SchemeRequest {

    @NotBlank(message = "Scheme name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "State or jurisdiction is required")
    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @NotBlank(message = "Department is required")
    @Size(max = 160, message = "Department must be at most 160 characters")
    private String department;

    @Size(max = 2048, message = "Official URL must be at most 2048 characters")
    @jakarta.validation.constraints.Pattern(regexp = "(?i)^https?://[^\\s/]+(?:/[^\\s]*)?$", message = "Official URL must use HTTP or HTTPS")
    private String officialUrl;

    @Size(max = 255, message = "Source name must be at most 255 characters")
    private String sourceName;

    @jakarta.validation.constraints.PastOrPresent(message = "Verification date cannot be in the future")
    private Instant lastVerifiedAt;

    private List<@NotBlank(message = "Benefits cannot contain blank entries") @Size(max = 500) String> benefits;

    private List<@NotBlank(message = "Document names cannot be blank") @Size(max = 255) String> requiredDocuments;

    @NotNull(message = "Eligibility criteria are required")
    @Valid // This is crucial for nested validation
    private EligibilityCriteria eligibilityCriteria;
}
