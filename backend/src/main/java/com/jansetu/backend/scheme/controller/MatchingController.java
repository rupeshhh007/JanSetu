package com.jansetu.backend.scheme.controller;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.service.MatchingService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<SchemeResponse>> getRecommendations(@Parameter(hidden = true)@AuthenticationPrincipal User user) {
        // We pass the authenticated user object directly to the service
        List<SchemeResponse> eligibleSchemes = matchingService.getEligibleSchemes(user);
        return ResponseEntity.ok(eligibleSchemes);
    }
}