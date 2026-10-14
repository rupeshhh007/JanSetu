package com.jansetu.backend.profile.controller;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.dto.ProfileRequest;
import com.jansetu.backend.profile.dto.ProfileResponse;
import com.jansetu.backend.profile.dto.ProfileFactRequest;
import com.jansetu.backend.profile.dto.ProfileFactResponse;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.profile.fact.CitizenProfileFactService;
import com.jansetu.backend.profile.service.CitizenProfileService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/citizen/profile")
public class CitizenProfileController {

    private final CitizenProfileService profileService;
    private final CitizenProfileFactService profileFactService;

    public CitizenProfileController(CitizenProfileService profileService, CitizenProfileFactService profileFactService) {
        this.profileService = profileService;
        this.profileFactService = profileFactService;
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
            @Parameter(hidden = true)@AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.updateProfile(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/facts")
    public ResponseEntity<List<ProfileFactResponse>> getProfileFacts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileFactService.factsForCitizen(user).stream()
                .map(this::mapFact)
                .toList());
    }

    @PutMapping("/facts/{factKey}")
    public ResponseEntity<ProfileFactResponse> submitProfileFact(
            @AuthenticationPrincipal User user,
            @PathVariable FactKey factKey,
            @Valid @RequestBody ProfileFactRequest request
    ) {
        return ResponseEntity.ok(mapFact(profileFactService.submitCitizenFact(
                user,
                factKey,
                request.value(),
                request.consentToCaseworkerReuse(),
                request.expiresAt()
        )));
    }

    private ProfileFactResponse mapFact(com.jansetu.backend.profile.fact.CitizenProfileFact fact) {
        return ProfileFactResponse.from(fact, profileFactService.isActive(fact));
    }
}
