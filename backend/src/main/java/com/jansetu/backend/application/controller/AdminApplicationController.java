package com.jansetu.backend.application.controller;

import com.jansetu.backend.application.dto.ApplicationResponse;
import com.jansetu.backend.application.dto.ApplicationStatusUpdateRequest;
import com.jansetu.backend.application.service.ApplicationService;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

/** Staff-only case-management endpoints; access is enforced in SecurityConfig. */
@RestController
@RequestMapping("/api/v1/admin/applications")
public class AdminApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public AdminApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplications() {
        return ResponseEntity.ok(applicationService.getAllApplicationsForAdmin());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        User reviewer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(applicationService.updateStatus(id, request, reviewer));
    }
}
