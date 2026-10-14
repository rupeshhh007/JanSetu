package com.jansetu.backend.privacy.api;

import com.jansetu.backend.privacy.CitizenDataExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Downloadable self-service personal data, kept out of intermediate caches. */
@RestController
@RequestMapping("/api/v1/citizen")
public class CitizenDataExportController {
    private final CitizenDataExportService exports;

    public CitizenDataExportController(CitizenDataExportService exports) {
        this.exports = exports;
    }

    @GetMapping(value = "/data-export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CitizenDataExportResponse> download() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"jansetu-my-data.json\"")
                .body(exports.exportMine());
    }
}
