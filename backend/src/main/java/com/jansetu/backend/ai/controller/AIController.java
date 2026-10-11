package com.jansetu.backend.ai.controller;

import com.jansetu.backend.ai.context.UserContext;
import com.jansetu.backend.ai.dto.ChatRequest;
import com.jansetu.backend.ai.dto.ChatResponse;
import com.jansetu.backend.ai.dto.ExplainRequest;
import com.jansetu.backend.ai.dto.ExplainResponse;
import com.jansetu.backend.ai.service.AIService;
import com.jansetu.backend.auth.entity.User;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/explain")
    public ExplainResponse explainEligibility(
            @RequestBody ExplainRequest request
    ) {

        return new ExplainResponse(
                aiService.explainEligibility(request)
        );
    }

    @PostMapping("/chat")
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request
    ) {
        return ChatResponse.from(aiService.chat(request));
    }

    @GetMapping("/context")
    public UserContext context() {
        return aiService.getContext();
    }
}
