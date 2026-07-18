package com.jansetu.backend.profile.controller;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.dto.ProfileRequest;
import com.jansetu.backend.profile.dto.ProfileResponse;
import com.jansetu.backend.profile.service.CitizenProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citizen/profile")
public class CitizenProfileController {

    private final CitizenProfileService profileService;

    public CitizenProfileController(CitizenProfileService profileService) {
        this.profileService = profileService;
    }

    // --- 1. CREATE PROFILE ---
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.createProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 2. GET PROFILE ---
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {

        ProfileResponse response = profileService.getProfile(user);
        return ResponseEntity.ok(response);
    }

    // --- 3. UPDATE PROFILE ---
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.updateProfile(user, request);
        return ResponseEntity.ok(response);
    }
}