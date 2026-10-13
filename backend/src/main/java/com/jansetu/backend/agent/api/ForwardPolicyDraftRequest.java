package com.jansetu.backend.agent.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ForwardPolicyDraftRequest(@NotNull LocalDate effectiveFrom) { }
