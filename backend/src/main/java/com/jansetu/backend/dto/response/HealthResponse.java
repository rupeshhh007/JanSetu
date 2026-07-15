package com.jansetu.backend.dto.response;

public record HealthResponse(
        String status,
        String application,
        String version
) {}