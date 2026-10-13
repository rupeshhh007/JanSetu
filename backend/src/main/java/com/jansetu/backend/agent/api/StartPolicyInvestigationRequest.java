package com.jansetu.backend.agent.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartPolicyInvestigationRequest(@NotNull UUID candidateId, @NotNull UUID schemeId) { }
