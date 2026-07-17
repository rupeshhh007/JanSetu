package com.jansetu.backend.auth.dto;

public record RegisterRequest(
        String email,
        String password
) {}