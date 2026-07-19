package com.irp.core.agent;

import com.irp.core.agent.dto.CreateAgentSuggestionRequest;
import com.irp.core.agent.dto.ReviewAgentSuggestionRequest;
import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.ConflictException;
import com.irp.core.common.exception.ResourceNotFoundException;
import com.irp.core.incident.Incident;
import com.irp.core.incident.IncidentRepository;
import com.irp.core.incident.IncidentStatus;
import com.irp.core.incident.IncidentTimelineEntry;
import com.irp.core.incident.IncidentTimelineEntryRepository;
import com.irp.core.incident.TimelineEntryType;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentSuggestionService {

    private final AgentSuggestionRepository agentSuggestionRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentTimelineEntryRepository timelineEntryRepository;
    private final ProjectService projectService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Called from the agent-facing controller, where projectId/organizationId already come
     * from a verified ApiKeyPrincipal rather than a caller-supplied path segment - so unlike
     * the dashboard-facing methods below, there's no separate org-ownership check to make here.
     */
    @Transactional
    public AgentSuggestion createSuggestion(UUID organizationId, UUID projectId, UUID incidentId,
                                             CreateAgentSuggestionRequest request) {
        Incident incident = getIncidentForProject(projectId, incidentId);

        AgentSuggestion suggestion = agentSuggestionRepository.save(new AgentSuggestion(
                incidentId, projectId, request.rootCause(), request.confidence(),
                request.evidence(), request.recommendedActions()));

        incident.changeStatus(IncidentStatus.AWAITING_APPROVAL);
        incidentRepository.save(incident);

        timelineEntryRepository.save(new IncidentTimelineEntry(incidentId, "agent", TimelineEntryType.AGENT_ACTION,
                "Proposed root cause (confidence %.0f%%): %s".formatted(
                        request.confidence().doubleValue() * 100, request.rootCause())));

        auditService.record(organizationId, projectId, "agent", "AGENT_SUGGESTION_CREATED", "Incident",
                incidentId.toString(), Map.of(
                        "suggestionId", suggestion.getId().toString(),
                        "confidence", request.confidence().toString()));

        return suggestion;
    }

    @Transactional(readOnly = true)
    public List<AgentSuggestion> listSuggestions(UUID organizationId, UUID projectId, UUID incidentId) {
        projectService.getProject(organizationId, projectId);
        getIncidentForProject(projectId, incidentId);
        return agentSuggestionRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);
    }

    /** Backs the AI Copilot page's project-wide queue - every prior page here paginated per
     * incident; this is the first cross-incident suggestion read. */
    @Transactional(readOnly = true)
    public List<AgentSuggestion> listSuggestionsForProject(UUID organizationId, UUID projectId,
                                                            Optional<AgentSuggestionStatus> status) {
        projectService.getProject(organizationId, projectId);
        return status
                .map(s -> agentSuggestionRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, s))
                .orElseGet(() -> agentSuggestionRepository.findByProjectIdOrderByCreatedAtDesc(projectId));
    }

    @Transactional
    public AgentSuggestion reviewSuggestion(UUID organizationId, UUID projectId, UUID incidentId, UUID suggestionId,
                                             String actor, ReviewAgentSuggestionRequest request) {
        projectService.getProject(organizationId, projectId);
        Incident incident = getIncidentForProject(projectId, incidentId);

        AgentSuggestion suggestion = agentSuggestionRepository.findByIdAndProjectId(suggestionId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentSuggestion", suggestionId));
        if (suggestion.getStatus() != AgentSuggestionStatus.PENDING_REVIEW) {
            throw new ConflictException("This suggestion has already been reviewed");
        }

        AgentSuggestionStatus decision = AgentSuggestionStatus.valueOf(request.decision());
        suggestion.review(decision, actor);
        agentSuggestionRepository.save(suggestion);

        incident.changeStatus(IncidentStatus.INVESTIGATING);
        incidentRepository.save(incident);

        String verb = decision == AgentSuggestionStatus.APPROVED ? "Approved" : "Rejected";
        String message = request.note() != null && !request.note().isBlank()
                ? "%s the agent's suggestion: %s".formatted(verb, request.note())
                : "%s the agent's suggestion".formatted(verb);
        timelineEntryRepository.save(new IncidentTimelineEntry(incidentId, actor, TimelineEntryType.APPROVAL, message));

        auditService.record(organizationId, projectId, actor, "AGENT_SUGGESTION_REVIEWED", "Incident",
                incidentId.toString(), Map.of("suggestionId", suggestionId.toString(), "decision", decision.name()));

        if (decision == AgentSuggestionStatus.APPROVED) {
            eventPublisher.publishEvent(new AgentSuggestionApprovedEvent(
                    incidentId, projectId, organizationId, incident.getTitle(), suggestion.getRootCause(), actor));
        }

        return suggestion;
    }

    private Incident getIncidentForProject(UUID projectId, UUID incidentId) {
        return incidentRepository.findByIdAndProjectId(incidentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));
    }
}
