package com.jansetu.backend.controller;

import com.jansetu.backend.dto.response.HealthResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Liveness: tells the platform whether HTTP is serving, independent of DB health.
    @GetMapping("/api/v1/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "JanSetu", "2.0.0");
    }

    // Readiness: keep DB details private, but do not advertise ready when PostgreSQL is down.
    @GetMapping("/api/v1/health/ready")
    public ResponseEntity<Map<String, String>> ready() {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            if (Integer.valueOf(1).equals(one)) {
                return ResponseEntity.ok(Map.of("status", "UP"));
            }
        } catch (DataAccessException exception) {
            // No credentials, exception messages, or database identifiers in public responses.
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "NOT_READY"));
    }
}
