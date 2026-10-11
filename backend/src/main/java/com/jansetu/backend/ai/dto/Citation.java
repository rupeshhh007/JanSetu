package com.jansetu.backend.ai.dto;

/** A source attached to a grounded assistant response. */
public record Citation(
        String title,
        String sourceName,
        String url,
        String lastVerifiedAt
) { }
