package com.irp.core.agent.runbook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RunbookSearchRequest(

        @NotBlank(message = "query is required")
        String query,

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 20, message = "topK must be at most 20")
        Integer topK
) {
    public int topKOrDefault() {
        return topK != null ? topK : 5;
    }
}
