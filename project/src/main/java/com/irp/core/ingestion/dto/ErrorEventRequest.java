package com.irp.core.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record ErrorEventRequest(

        @NotNull(message = "occurredAt is required")
        Instant occurredAt,

        @NotBlank(message = "service is required")
        String service,

        @NotBlank(message = "exceptionType is required")
        String exceptionType,

        String message,

        String stackTrace,

        Map<String, Object> metadata
) {
}
