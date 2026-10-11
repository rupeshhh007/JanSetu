package com.jansetu.backend.source.api;

import com.jansetu.backend.source.OfficialSourceCreateCommand;
import com.jansetu.backend.source.SourceSnapshotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Administrator-only source intelligence API; /api/v1/admin is protected by SecurityConfig. */
@RestController
@RequestMapping("/api/v1/admin/sources")
public class OfficialSourceController {
    private final SourceSnapshotService sourceSnapshots;

    public OfficialSourceController(SourceSnapshotService sourceSnapshots) {
        this.sourceSnapshots = sourceSnapshots;
    }

    @PostMapping
    public ResponseEntity<OfficialSourceResponse> register(@Valid @RequestBody OfficialSourceRequest request) {
        var source = sourceSnapshots.register(new OfficialSourceCreateCommand(request.name(), request.authority(), request.canonicalUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).body(OfficialSourceResponse.from(source));
    }

    @GetMapping
    public List<OfficialSourceResponse> list() {
        return sourceSnapshots.listSources().stream().map(OfficialSourceResponse::from).toList();
    }

    @PostMapping("/{sourceId}/snapshots")
    public ResponseEntity<SourceSnapshotResponse> capture(@PathVariable UUID sourceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SourceSnapshotResponse.from(sourceSnapshots.capture(sourceId)));
    }

    @GetMapping("/{sourceId}/snapshots")
    public List<SourceSnapshotResponse> history(@PathVariable UUID sourceId) {
        return sourceSnapshots.history(sourceId).stream().map(SourceSnapshotResponse::from).toList();
    }

    @PostMapping("/{sourceId}/disable")
    public OfficialSourceResponse disable(@PathVariable UUID sourceId) {
        return OfficialSourceResponse.from(sourceSnapshots.disable(sourceId));
    }
}
