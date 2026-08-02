package com.jansetu.backend.application.dto;

import com.jansetu.backend.application.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {
    private UUID applicationId;
    private UUID schemeId; // <--- Add this line
    private String schemeName;
    private ApplicationStatus status;
    private String remarks;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}