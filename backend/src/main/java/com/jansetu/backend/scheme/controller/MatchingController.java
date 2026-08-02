package com.jansetu.backend.scheme.controller;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching API", description = "Endpoints for scheme recommendations")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get personalized scheme recommendations")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal User user // <-- Extracts the currently logged-in user!
    ) {
        // Now we pass the user directly to your service
        List<RecommendationResponse> recommendations = matchingService.getRecommendations(user);
        return ResponseEntity.ok(recommendations);
    }
}