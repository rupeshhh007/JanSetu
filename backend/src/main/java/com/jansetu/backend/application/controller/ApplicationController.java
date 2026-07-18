package com.jansetu.backend.application.controller;

import com.jansetu.backend.application.dto.ApplicationRequest;
import com.jansetu.backend.application.dto.ApplicationResponse;
import com.jansetu.backend.application.service.ApplicationService;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository; // Add this
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository; // Add this

    public ApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> submitApplication(
            @Parameter(hidden = true)@AuthenticationPrincipal UserDetails userDetails, // Change to UserDetails
            @Valid @RequestBody ApplicationRequest request) {

        // 1. Manually look up the User entity from the DB
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Now pass the loaded User entity to the service
        return ResponseEntity.ok(applicationService.submitApplication(user, request));
    }
}