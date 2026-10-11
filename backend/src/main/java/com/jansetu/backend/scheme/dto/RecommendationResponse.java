package com.jansetu.backend.scheme.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RecommendationResponse {

    // The actual scheme data so the frontend can reuse their SchemeCard
    private SchemeResponse scheme;

    // The context for the UI
    private int matchPercentage;
    private List<String> matchedCriteria;

    /** Deterministic rules outcome; this is not an AI confidence score. */
    private String eligibilityStatus;
    private List<String> missingInformation;
}
