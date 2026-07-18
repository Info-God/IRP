package com.irp.core.incident.dto;

import com.irp.core.incident.IncidentTimelineEntry;

import java.time.Instant;
import java.util.UUID;

public record IncidentTimelineEntryResponse(
        UUID id,
        String actor,
        String entryType,
        String message,
        Instant createdAt
) {
    public static IncidentTimelineEntryResponse from(IncidentTimelineEntry entry) {
        return new IncidentTimelineEntryResponse(entry.getId(), entry.getActor(),
                entry.getEntryType().name(), entry.getMessage(), entry.getCreatedAt());
    }
}
