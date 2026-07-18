package com.irp.core.agent;

import com.irp.core.agent.dto.AgentSuggestionResponse;
import com.irp.core.agent.dto.ReviewAgentSuggestionRequest;
import com.irp.core.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Dashboard-facing: list an incident's agent suggestions and approve/reject them (JWT-only, see SecurityConfig). */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/incidents/{incidentId}/agent-suggestions")
@RequiredArgsConstructor
public class AgentSuggestionController {

    private final AgentSuggestionService agentSuggestionService;

    @GetMapping
    public List<AgentSuggestionResponse> listSuggestions(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                           @PathVariable UUID projectId,
                                                           @PathVariable UUID incidentId) {
        return agentSuggestionService.listSuggestions(currentUser.getOrganizationId(), projectId, incidentId).stream()
                .map(AgentSuggestionResponse::from)
                .toList();
    }

    @PatchMapping("/{suggestionId}")
    public AgentSuggestionResponse reviewSuggestion(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                     @PathVariable UUID projectId,
                                                     @PathVariable UUID incidentId,
                                                     @PathVariable UUID suggestionId,
                                                     @Valid @RequestBody ReviewAgentSuggestionRequest request) {
        AgentSuggestion suggestion = agentSuggestionService.reviewSuggestion(
                currentUser.getOrganizationId(), projectId, incidentId, suggestionId, currentUser.getUsername(), request);
        return AgentSuggestionResponse.from(suggestion);
    }
}
