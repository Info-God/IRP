package com.irp.core.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

public record ErrorEventRequest(

        @NotNull(message = "occurredAt is required")
        Instant occurredAt,

        @NotBlank(message = "service is required")
        @Size(max = 150)
        String service,

        @NotBlank(message = "exceptionType is required")
        @Size(max = 255)
        String exceptionType,

        String message,

        String stackTrace,

        Map<String, Object> metadata
) {
}
