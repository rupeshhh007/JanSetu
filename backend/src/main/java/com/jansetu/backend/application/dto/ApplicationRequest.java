package com.jansetu.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ApplicationRequest {
    @NotNull(message = "Scheme ID is required")
    private UUID schemeId;
}