package com.irp.core.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

public record CreateAgentStepRequest(
        @NotBlank(message = "toolName is required")
        String toolName,

        JsonNode toolInput,
        JsonNode toolOutput
) {
}
