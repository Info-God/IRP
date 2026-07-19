package com.irp.core.agent;

import java.util.UUID;

/** Published after an approved suggestion's transaction commits. Listened to by
 * {@link com.irp.core.integrations.slack.SlackNotificationListener} to post a notification
 * if the project has a Slack integration configured - mirrors how
 * {@link com.irp.core.incident.IncidentCreatedEvent} decouples Phase 1's auto-trigger from
 * IncidentService. */
public record AgentSuggestionApprovedEvent(
        UUID incidentId,
        UUID projectId,
        UUID organizationId,
        String incidentTitle,
        String rootCause,
        String approvedBy
) {
}
