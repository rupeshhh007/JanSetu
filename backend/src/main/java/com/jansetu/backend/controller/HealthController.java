package com.jansetu.backend.controller;

import com.jansetu.backend.dto.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public HealthResponse health() {
        return new HealthResponse(
                "UP",
                "JanSetu",
                "1.0.0"
        );
    }
}