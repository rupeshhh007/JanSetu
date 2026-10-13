package com.jansetu.backend.preparation.api;

import jakarta.validation.constraints.NotNull;

public record PreparationTaskUpdateRequest(@NotNull Boolean complete) { }
