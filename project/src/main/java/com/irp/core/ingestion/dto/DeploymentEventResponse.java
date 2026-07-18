package com.irp.core.ingestion.dto;

import com.irp.core.ingestion.DeploymentEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DeploymentEventResponse(
        UUID id,
        UUID projectId,
        Instant occurredAt,
        Instant receivedAt,
        String service,
        String version,
        String status,
        Map<String, Object> metadata
) {
    public static DeploymentEventResponse from(DeploymentEvent event) {
        return new DeploymentEventResponse(event.getId(), event.getProjectId(), event.getOccurredAt(), event.getReceivedAt(),
                event.getService(), event.getVersion(), event.getStatus().name(), event.getMetadata());
    }
}
