package com.irp.core.agent;

import com.irp.core.agent.dto.AgentSuggestionResponse;
import com.irp.core.agent.dto.CreateAgentSuggestionRequest;
import com.irp.core.common.web.PageResponse;
import com.irp.core.incident.Incident;
import com.irp.core.incident.IncidentService;
import com.irp.core.incident.dto.IncidentDetailResponse;
import com.irp.core.incident.dto.IncidentResponse;
import com.irp.core.incident.dto.IncidentTimelineEntryResponse;
import com.irp.core.ingestion.IngestionService;
import com.irp.core.ingestion.dto.DeploymentEventResponse;
import com.irp.core.ingestion.dto.ErrorEventResponse;
import com.irp.core.ingestion.dto.LogEventResponse;
import com.irp.core.security.apikey.ApiKeyPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Called by the AI agent service, authenticated the same way the SDK is - a project-scoped
 * X-API-Key (see {@link com.irp.core.security.apikey.ApiKeyAuthenticationFilter}) - never by
 * the dashboard. Every project/org value used here comes from the authenticated key itself,
 * not from the URL, so there is nothing for a caller to spoof by editing a path segment.
 */
@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    private final IngestionService ingestionService;
    private final IncidentService incidentService;
    private final AgentSuggestionService agentSuggestionService;

    @GetMapping("/logs")
    public PageResponse<LogEventResponse> searchLogs(@AuthenticationPrincipal ApiKeyPrincipal principal,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String level,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 100) Pageable pageable) {
        return PageResponse.from(ingestionService
                .searchLogs(principal.projectId(), service, level, from, to, pageable)
                .map(LogEventResponse::from));
    }

    @GetMapping("/errors")
    public PageResponse<ErrorEventResponse> searchErrors(@AuthenticationPrincipal ApiKeyPrincipal principal,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stackHash,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 100) Pageable pageable) {
        return PageResponse.from(ingestionService
                .searchErrors(principal.projectId(), service, stackHash, from, to, pageable)
                .map(ErrorEventResponse::from));
    }

    @GetMapping("/deployments")
    public PageResponse<DeploymentEventResponse> searchDeployments(@AuthenticationPrincipal ApiKeyPrincipal principal,
            @RequestParam(required = false) String service,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 50) Pageable pageable) {
        return PageResponse.from(ingestionService
                .searchDeployments(principal.projectId(), service, from, to, pageable)
                .map(DeploymentEventResponse::from));
    }

    @GetMapping("/incidents/{incidentId}")
    public IncidentDetailResponse getIncident(@AuthenticationPrincipal ApiKeyPrincipal principal,
                                               @PathVariable UUID incidentId) {
        Incident incident = incidentService.getIncident(principal.organizationId(), principal.projectId(), incidentId);
        var timeline = incidentService.getTimeline(principal.organizationId(), principal.projectId(), incidentId).stream()
                .map(IncidentTimelineEntryResponse::from)
                .toList();
        return new IncidentDetailResponse(IncidentResponse.from(incident), timeline);
    }

    @PostMapping("/incidents/{incidentId}/suggestions")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentSuggestionResponse createSuggestion(@AuthenticationPrincipal ApiKeyPrincipal principal,
                                                     @PathVariable UUID incidentId,
                                                     @Valid @RequestBody CreateAgentSuggestionRequest request) {
        AgentSuggestion suggestion = agentSuggestionService.createSuggestion(
                principal.organizationId(), principal.projectId(), incidentId, request);
        return AgentSuggestionResponse.from(suggestion);
    }
}
