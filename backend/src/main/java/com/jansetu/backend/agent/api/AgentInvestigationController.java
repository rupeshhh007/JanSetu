package com.jansetu.backend.agent.api;

import com.jansetu.backend.agent.service.AgentExecutionService;
import com.jansetu.backend.auth.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/ai-investigations")
public class AgentInvestigationController {
    private final AgentExecutionService executions;

    public AgentInvestigationController(AgentExecutionService executions) {
        this.executions = executions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyAgentRunResponse start(@Valid @RequestBody StartPolicyInvestigationRequest request,
                                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                                        @AuthenticationPrincipal User actor) {
        return PolicyAgentRunResponse.from(executions.start(
                request.candidateId(), request.schemeId(), request.resolvedMode(), idempotencyKey, actor));
    }

    @GetMapping
    public List<PolicyAgentRunResponse> history(@AuthenticationPrincipal User actor) {
        return executions.history(actor).stream().map(PolicyAgentRunResponse::from).toList();
    }

    @GetMapping("/{runId}")
    public PolicyAgentRunResponse get(@PathVariable UUID runId, @AuthenticationPrincipal User actor) {
        return PolicyAgentRunResponse.from(executions.get(runId, actor));
    }

    @GetMapping("/{runId}/events")
    public List<PolicyAgentEventResponse> timeline(@PathVariable UUID runId,
                                                   @AuthenticationPrincipal User actor) {
        return executions.timeline(runId, actor).stream().map(PolicyAgentEventResponse::from).toList();
    }

    @PostMapping("/{runId}/cancel")
    public PolicyAgentRunResponse cancel(@PathVariable UUID runId, @AuthenticationPrincipal User actor) {
        return PolicyAgentRunResponse.from(executions.cancel(runId, actor));
    }
}
