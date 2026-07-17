package com.jansetu.backend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}