package com.irp.core.agent.runbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRunbookRequest(

        @NotBlank(message = "title is required")
        @Size(max = 255)
        String title,

        @NotBlank(message = "content is required")
        String content
) {
}
