package com.jansetu.backend.application.dto;

import com.jansetu.backend.application.enums.ApplicationStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ApplicationStatusHistoryResponse(
        UUID id,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        String remarks,
        String changedBy,
        Instant changedAt
) {}
