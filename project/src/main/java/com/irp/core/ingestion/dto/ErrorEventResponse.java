package com.irp.core.ingestion.dto;

import com.irp.core.ingestion.ErrorEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ErrorEventResponse(
        UUID id,
        UUID projectId,
        Instant occurredAt,
        Instant receivedAt,
        String service,
        String exceptionType,
        String message,
        String stackTrace,
        String stackHash,
        Map<String, Object> metadata
) {
    public static ErrorEventResponse from(ErrorEvent event) {
        return new ErrorEventResponse(event.getId(), event.getProjectId(), event.getOccurredAt(), event.getReceivedAt(),
                event.getService(), event.getExceptionType(), event.getMessage(), event.getStackTrace(),
                event.getStackHash(), event.getMetadata());
    }
}
