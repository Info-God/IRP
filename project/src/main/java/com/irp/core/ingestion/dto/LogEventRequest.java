package com.irp.core.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

public record LogEventRequest(

        @NotNull(message = "occurredAt is required")
        Instant occurredAt,

        @NotBlank(message = "level is required")
        @Pattern(regexp = "TRACE|DEBUG|INFO|WARN|ERROR", message = "level must be one of TRACE, DEBUG, INFO, WARN, ERROR")
        String level,

        @NotBlank(message = "service is required")
        @Size(max = 150)
        String service,

        @NotBlank(message = "message is required")
        String message,

        @Size(max = 100)
        String traceId,

        Map<String, Object> metadata
) {
}
