package com.irp.core.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.irp.core.agent.AgentStep;

import java.time.Instant;
import java.util.UUID;

public record AgentStepResponse(
        UUID id,
        int stepIndex,
        String toolName,
        JsonNode toolInput,
        JsonNode toolOutput,
        Instant createdAt
) {
    public static AgentStepResponse from(AgentStep step) {
        return new AgentStepResponse(step.getId(), step.getStepIndex(), step.getToolName(),
                step.getToolInput(), step.getToolOutput(), step.getCreatedAt());
    }
}
