package com.jansetu.backend.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String message,
        String role
) {}