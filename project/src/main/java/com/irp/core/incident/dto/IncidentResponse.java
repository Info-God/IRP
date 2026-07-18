package com.irp.core.incident.dto;

import com.irp.core.incident.Incident;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        String severity,
        String status,
        String service,
        Instant openedAt,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(), incident.getProjectId(), incident.getTitle(), incident.getDescription(),
                incident.getSeverity().name(), incident.getStatus().name(), incident.getService(),
                incident.getOpenedAt(), incident.getResolvedAt(), incident.getCreatedAt(), incident.getUpdatedAt());
    }
}
