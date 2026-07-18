package com.irp.core.incident;

import java.util.UUID;

/**
 * Published after an incident is committed. Listened to by
 * {@link com.irp.core.agent.AgentInvocationListener} to auto-trigger an AI investigation.
 */
public record IncidentCreatedEvent(UUID incidentId, UUID projectId, UUID organizationId) {
}
