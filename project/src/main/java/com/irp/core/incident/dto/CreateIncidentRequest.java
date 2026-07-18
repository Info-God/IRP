package com.irp.core.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(

        @NotBlank(message = "title is required")
        @Size(max = 255)
        String title,

        String description,

        @NotBlank(message = "severity is required")
        @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "severity must be one of LOW, MEDIUM, HIGH, CRITICAL")
        String severity,

        @Size(max = 150)
        String service
) {
}
