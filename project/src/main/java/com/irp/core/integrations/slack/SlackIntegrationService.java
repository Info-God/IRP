package com.irp.core.integrations.slack;

import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.BadRequestException;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlackIntegrationService {

    private final SlackIntegrationRepository slackIntegrationRepository;
    private final ProjectService projectService;
    private final AuditService auditService;
    private final SlackNotifier slackNotifier;

    /** Upsert: connecting again (e.g. rotating the webhook) just replaces the stored URL -
     * a project has at most one Slack integration. */
    @Transactional
    public SlackIntegration connect(UUID organizationId, UUID projectId, String actor, String webhookUrl) {
        projectService.getProject(organizationId, projectId);

        SlackIntegration integration = slackIntegrationRepository.findByProjectId(projectId)
                .map(existing -> {
                    existing.reconnect(webhookUrl, actor);
                    return existing;
                })
                .orElseGet(() -> new SlackIntegration(projectId, webhookUrl, actor));
        integration = slackIntegrationRepository.save(integration);

        // Never record the webhook URL itself in the audit trail - it's a bearer secret.
        auditService.record(organizationId, projectId, actor, "SLACK_INTEGRATION_CONNECTED",
                "SlackIntegration", integration.getId().toString(), Map.of());

        return integration;
    }

    @Transactional(readOnly = true)
    public Optional<SlackIntegration> get(UUID organizationId, UUID projectId) {
        projectService.getProject(organizationId, projectId);
        return slackIntegrationRepository.findByProjectId(projectId);
    }

    @Transactional
    public void disconnect(UUID organizationId, UUID projectId, String actor) {
        projectService.getProject(organizationId, projectId);
        slackIntegrationRepository.findByProjectId(projectId).ifPresent(integration -> {
            slackIntegrationRepository.delete(integration);
            auditService.record(organizationId, projectId, actor, "SLACK_INTEGRATION_DISCONNECTED",
                    "SlackIntegration", integration.getId().toString(), Map.of());
        });
    }

    /** Unlike the automatic on-approval notification, a failure here should reach the user -
     * they clicked a button specifically to find out whether the webhook works. */
    public void sendTestMessage(UUID organizationId, UUID projectId) {
        SlackIntegration integration = get(organizationId, projectId)
                .orElseThrow(() -> new BadRequestException("No Slack integration is configured for this project"));
        slackNotifier.send(integration.getWebhookUrl(),
                ":white_check_mark: Test message from IRP - your Slack integration is working.");
    }
}
