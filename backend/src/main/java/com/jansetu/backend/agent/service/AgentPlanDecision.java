package com.jansetu.backend.agent.service;

import com.jansetu.backend.agent.tool.AgentToolName;

public record AgentPlanDecision(AgentToolName tool, boolean modelCall, int estimatedTokens) { }
