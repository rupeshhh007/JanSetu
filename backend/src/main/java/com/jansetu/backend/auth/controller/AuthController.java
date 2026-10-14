package com.jansetu.backend.auth.controller;

import com.jansetu.backend.auth.dto.LoginRequest;
import com.jansetu.backend.auth.dto.LoginResponse;
import com.jansetu.backend.auth.dto.RegisterRequest;
import com.jansetu.backend.auth.dto.RegisterResponse;
import com.jansetu.backend.auth.service.AuthService;
import com.jansetu.backend.auth.security.AuthenticationRateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationRateLimiter rateLimiter;

    // Constructor Injection
    public AuthController(AuthService authService, AuthenticationRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        rateLimiter.checkIdentity(request.email(), true);
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        rateLimiter.checkIdentity(request.email(), false);
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}