package com.irp.core.agent;

import com.irp.core.agent.dto.AgentRunDetailResponse;
import com.irp.core.agent.dto.AgentRunResponse;
import com.irp.core.agent.dto.AgentStepResponse;
import com.irp.core.security.principal.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Dashboard-facing: the AI agent's per-run tool-call trace for an incident (JWT-only, see SecurityConfig). */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/incidents/{incidentId}/agent-runs")
@RequiredArgsConstructor
public class AgentRunController {

    private final AgentRunService agentRunService;

    @GetMapping
    public List<AgentRunDetailResponse> listRuns(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                  @PathVariable UUID projectId,
                                                  @PathVariable UUID incidentId) {
        return agentRunService.listRuns(currentUser.getOrganizationId(), projectId, incidentId).stream()
                .map(run -> new AgentRunDetailResponse(
                        AgentRunResponse.from(run),
                        agentRunService.listSteps(run.getId()).stream().map(AgentStepResponse::from).toList()))
                .toList();
    }
}
