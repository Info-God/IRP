package com.irp.core.integrations.slack;

import com.irp.core.agent.AgentSuggestionApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Posts to Slack when an AI suggestion is approved, if the project has a webhook configured.
 * Runs async, after the approval's own transaction commits, and never lets a Slack failure
 * propagate - same shape as {@link com.irp.core.agent.AgentInvocationListener}'s auto-trigger:
 * the thing that happened (the approval) must never depend on the thing it's telling someone
 * about (Slack) being reachable.
 */
@Component
@RequiredArgsConstructor
public class SlackNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(SlackNotificationListener.class);

    private final SlackIntegrationRepository slackIntegrationRepository;
    private final SlackNotifier slackNotifier;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSuggestionApproved(AgentSuggestionApprovedEvent event) {
        slackIntegrationRepository.findByProjectId(event.projectId()).ifPresent(integration -> {
            String text = ":white_check_mark: *%s* approved by %s\n> %s".formatted(
                    event.incidentTitle(), event.approvedBy(), event.rootCause());
            try {
                slackNotifier.send(integration.getWebhookUrl(), text);
            } catch (Exception e) {
                log.warn("Failed to send Slack notification for incident {}: {}", event.incidentId(), e.getMessage());
            }
        });
    }
}
