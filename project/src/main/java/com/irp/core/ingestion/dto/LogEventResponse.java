package com.irp.core.ingestion.dto;

import com.irp.core.ingestion.LogEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LogEventResponse(
        UUID id,
        UUID projectId,
        Instant occurredAt,
        Instant receivedAt,
        String level,
        String service,
        String message,
        String traceId,
        Map<String, Object> metadata
) {
    public static LogEventResponse from(LogEvent event) {
        return new LogEventResponse(event.getId(), event.getProjectId(), event.getOccurredAt(), event.getReceivedAt(),
                event.getLevel().name(), event.getService(), event.getMessage(), event.getTraceId(), event.getMetadata());
    }
}
