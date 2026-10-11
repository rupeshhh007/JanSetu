package com.jansetu.backend.application.dto;

import com.jansetu.backend.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationStatusUpdateRequest(
        @NotNull(message = "A target status is required") ApplicationStatus status,
        @NotBlank(message = "A review remark is required")
        @Size(max = 2000, message = "Remarks must be at most 2000 characters") String remarks
) {}
