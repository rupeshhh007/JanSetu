package com.jansetu.backend.preparation.api;

import com.jansetu.backend.preparation.ApplicationPreparationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preparations")
public class ApplicationPreparationController {
    private final ApplicationPreparationService preparations;

    public ApplicationPreparationController(ApplicationPreparationService preparations) {
        this.preparations = preparations;
    }

    @PostMapping("/schemes/{schemeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationPreparationResponse start(@PathVariable UUID schemeId) { return preparations.start(schemeId); }

    @GetMapping
    public List<ApplicationPreparationResponse> mine() { return preparations.mine(); }

    @GetMapping("/{sessionId}")
    public ApplicationPreparationResponse get(@PathVariable UUID sessionId) { return preparations.get(sessionId); }

    @PatchMapping("/{sessionId}/tasks/{taskId}")
    public ApplicationPreparationResponse updateTask(@PathVariable UUID sessionId, @PathVariable UUID taskId,
                                                      @Valid @RequestBody PreparationTaskUpdateRequest request) {
        return preparations.updateTask(sessionId, taskId, request.complete());
    }

    @PostMapping("/{sessionId}/report-applied")
    public ApplicationPreparationResponse reportApplied(@PathVariable UUID sessionId) {
        return preparations.reportApplied(sessionId);
    }
}
