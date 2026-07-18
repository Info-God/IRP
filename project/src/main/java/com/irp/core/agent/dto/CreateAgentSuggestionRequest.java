package com.irp.core.agent.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CreateAgentSuggestionRequest(

        @NotBlank(message = "rootCause is required")
        String rootCause,

        @NotNull(message = "confidence is required")
        @DecimalMin(value = "0.0", message = "confidence must be >= 0")
        @DecimalMax(value = "1.0", message = "confidence must be <= 1")
        BigDecimal confidence,

        @NotEmpty(message = "at least one evidence item is required - a confidence score must be grounded")
        List<Map<String, Object>> evidence,

        @NotNull(message = "recommendedActions is required (an empty list is fine if none apply)")
        List<Map<String, Object>> recommendedActions
) {
}
