package com.irp.core.integrations.slack.dto;

import com.irp.core.integrations.slack.SlackIntegration;

import java.time.Instant;
import java.util.UUID;

public record SlackIntegrationResponse(
        UUID id,
        String maskedWebhookUrl,
        String connectedBy,
        Instant createdAt,
        Instant updatedAt
) {
    private static final String SLACK_WEBHOOK_PREFIX = "https://hooks.slack.com/services/";

    public static SlackIntegrationResponse from(SlackIntegration integration) {
        return new SlackIntegrationResponse(integration.getId(), mask(integration.getWebhookUrl()),
                integration.getConnectedBy(), integration.getCreatedAt(), integration.getUpdatedAt());
    }

    private static String mask(String webhookUrl) {
        String tail = webhookUrl.length() >= 4 ? webhookUrl.substring(webhookUrl.length() - 4) : webhookUrl;
        return SLACK_WEBHOOK_PREFIX + "••••" + tail;
    }
}
