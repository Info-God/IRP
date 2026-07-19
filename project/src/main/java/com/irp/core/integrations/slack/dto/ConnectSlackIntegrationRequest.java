package com.irp.core.integrations.slack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConnectSlackIntegrationRequest(

        @NotBlank(message = "webhookUrl is required")
        @Size(max = 500)
        @Pattern(regexp = "^https://hooks\\.slack\\.com/services/.+",
                message = "webhookUrl must be a Slack incoming webhook URL (https://hooks.slack.com/services/...)")
        String webhookUrl
) {
}
