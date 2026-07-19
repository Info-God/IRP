package com.irp.core.integrations.slack;

import com.irp.core.integrations.slack.dto.ConnectSlackIntegrationRequest;
import com.irp.core.integrations.slack.dto.SlackIntegrationResponse;
import com.irp.core.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Dashboard-facing: connect/inspect/disconnect a project's Slack webhook (JWT-only, see SecurityConfig). */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/integrations/slack")
@RequiredArgsConstructor
public class SlackIntegrationController {

    private final SlackIntegrationService slackIntegrationService;

    @PutMapping
    public SlackIntegrationResponse connect(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                             @PathVariable UUID projectId,
                                             @Valid @RequestBody ConnectSlackIntegrationRequest request) {
        return SlackIntegrationResponse.from(slackIntegrationService.connect(
                currentUser.getOrganizationId(), projectId, currentUser.getUsername(), request.webhookUrl()));
    }

    @GetMapping
    public ResponseEntity<SlackIntegrationResponse> get(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                          @PathVariable UUID projectId) {
        return slackIntegrationService.get(currentUser.getOrganizationId(), projectId)
                .map(SlackIntegrationResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(@AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable UUID projectId) {
        slackIntegrationService.disconnect(currentUser.getOrganizationId(), projectId, currentUser.getUsername());
    }

    @PostMapping("/test")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTestMessage(@AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable UUID projectId) {
        slackIntegrationService.sendTestMessage(currentUser.getOrganizationId(), projectId);
    }
}
