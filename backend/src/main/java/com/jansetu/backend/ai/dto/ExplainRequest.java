package com.jansetu.backend.ai.dto;

import java.util.List;

public record ExplainRequest(
        String userProfileSummary,
        String schemeName,
        String schemeDescription,
        List<String> matchedCriteria
) {}