package com.irp.core.agent;

import com.irp.core.agent.dto.AgentSuggestionResponse;
import com.irp.core.security.principal.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Dashboard-facing: every AI suggestion for a project, across all incidents - the AI Copilot
 * queue's data source (JWT-only, see SecurityConfig). Separate from AgentSuggestionController,
 * which is scoped under a single incident. */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/agent-suggestions")
@RequiredArgsConstructor
public class ProjectAgentSuggestionController {

    private final AgentSuggestionService agentSuggestionService;

    @GetMapping
    public List<AgentSuggestionResponse> listSuggestions(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                           @PathVariable UUID projectId,
                                                           @RequestParam(required = false) String status) {
        Optional<AgentSuggestionStatus> statusFilter = Optional.ofNullable(status).map(AgentSuggestionStatus::valueOf);
        return agentSuggestionService.listSuggestionsForProject(currentUser.getOrganizationId(), projectId, statusFilter)
                .stream()
                .map(AgentSuggestionResponse::from)
                .toList();
    }
}
