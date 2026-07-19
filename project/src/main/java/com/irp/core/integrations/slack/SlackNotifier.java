package com.irp.core.integrations.slack;

import com.irp.core.common.exception.ServiceUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Posts to a Slack incoming webhook. Always throws on failure - callers decide whether that
 * should surface to a user (an explicit "send test message" click) or be logged and swallowed
 * (the automatic on-approval notification, which must never fail the approval itself).
 */
@Component
public class SlackNotifier {

    private final RestClient restClient;

    public SlackNotifier(RestClient.Builder restClientBuilder) {
        // No fixed base URL: the webhook URL is per-project user input, not a service this
        // backend owns, so every call passes its own full absolute URI.
        this.restClient = restClientBuilder.build();
    }

    public void send(String webhookUrl, String text) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new ServiceUnavailableException("Could not reach Slack: " + e.getMessage());
        }
    }
}
