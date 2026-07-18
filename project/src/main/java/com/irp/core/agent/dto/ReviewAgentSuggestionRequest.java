package com.irp.core.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReviewAgentSuggestionRequest(

        @NotBlank(message = "decision is required")
        @Pattern(regexp = "APPROVED|REJECTED", message = "decision must be one of APPROVED, REJECTED")
        String decision,

        String note
) {
}
