package com.irp.core.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.Map;

public record DeploymentEventRequest(

        @NotNull(message = "occurredAt is required")
        Instant occurredAt,

        @NotBlank(message = "service is required")
        String service,

        @NotBlank(message = "version is required")
        String version,

        @NotBlank(message = "status is required")
        @Pattern(regexp = "STARTED|SUCCEEDED|FAILED|ROLLED_BACK",
                message = "status must be one of STARTED, SUCCEEDED, FAILED, ROLLED_BACK")
        String status,

        Map<String, Object> metadata
) {
}
