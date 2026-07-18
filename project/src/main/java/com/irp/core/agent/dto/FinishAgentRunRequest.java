package com.irp.core.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record FinishAgentRunRequest(

        @NotBlank(message = "status is required")
        @Pattern(regexp = "SUCCEEDED|FAILED", message = "status must be one of SUCCEEDED, FAILED")
        String status,

        Map<String, Object> tokenUsage
) {
}
