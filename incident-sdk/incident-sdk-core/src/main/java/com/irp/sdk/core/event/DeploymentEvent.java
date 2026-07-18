package com.irp.sdk.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.Map;

/**
 * Field names match irp-core's DeploymentEventRequest exactly (occurredAt, service,
 * version, status, metadata). Unlike LOG and ERROR, irp-core's /ingest/deployments
 * endpoint takes ONE object per request, not a batch wrapper - HttpSender sends these
 * one at a time for that reason (see HttpSender.sendBatch).
 */
public record DeploymentEvent(
        Instant occurredAt,
        String service,
        String version,
        String status,
        Map<String, Object> metadata
) implements IncidentEvent {

    @Override
    @JsonIgnore
    public EventType type() {
        return EventType.DEPLOYMENT;
    }

    /** markDeployment(version, commitHash) has no status parameter - calling it at all implies
     *  the deploy finished, so it defaults to SUCCEEDED. commitHash isn't a backend column yet,
     *  so it travels in metadata instead. */
    public static DeploymentEvent of(String version, String commitHash, String service) {
        return new DeploymentEvent(Instant.now(), service, version, "SUCCEEDED", Map.of("commitHash", commitHash));
    }
}
