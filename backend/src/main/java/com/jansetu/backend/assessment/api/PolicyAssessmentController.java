package com.jansetu.backend.assessment.api;

import com.jansetu.backend.assessment.PolicyAssessmentService;
import com.jansetu.backend.auth.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Citizen-owned assessment endpoints. Ownership is checked in the service, never by a client-side identifier. */
@RestController
@RequestMapping("/api/v1/assessments")
public class PolicyAssessmentController {
    private final PolicyAssessmentService assessments;
    private final CurrentUserService users;

    public PolicyAssessmentController(PolicyAssessmentService assessments, CurrentUserService users) {
        this.assessments = assessments;
        this.users = users;
    }

    @PostMapping("/schemes/{schemeId}")
    public ResponseEntity<PolicyAssessmentResponse> assess(@PathVariable UUID schemeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PolicyAssessmentResponse.from(assessments.assessCurrent(users.getCurrentUser(), schemeId)));
    }

    @GetMapping
    public List<PolicyAssessmentResponse> history() {
        return assessments.history(users.getCurrentUser()).stream().map(PolicyAssessmentResponse::from).toList();
    }

    @GetMapping("/{assessmentId}/replay")
    public PolicyAssessmentReplayResponse replay(@PathVariable UUID assessmentId) {
        return PolicyAssessmentReplayResponse.from(assessments.replay(users.getCurrentUser(), assessmentId));
    }
}
