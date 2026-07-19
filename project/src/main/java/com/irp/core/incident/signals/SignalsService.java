package com.irp.core.incident.signals;

import com.irp.core.common.exception.ResourceNotFoundException;
import com.irp.core.incident.Incident;
import com.irp.core.incident.IncidentRepository;
import com.irp.core.incident.signals.dto.RelatedSignalsResponse;
import com.irp.core.ingestion.IngestionService;
import com.irp.core.ingestion.dto.DeploymentEventResponse;
import com.irp.core.ingestion.dto.ErrorEventResponse;
import com.irp.core.ingestion.dto.LogEventResponse;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Gives the dashboard (JWT) the same log/error/deployment reads the AI agent already has
 * (API-key, see AgentController) - reuses IngestionService's existing search methods rather
 * than duplicating query logic, just with a tenancy check and a window derived from the
 * incident's own opened_at instead of caller-supplied timestamps.
 */
@Service
@RequiredArgsConstructor
public class SignalsService {

    private static final int MAX_ITEMS_PER_CATEGORY = 50;

    private final IngestionService ingestionService;
    private final IncidentRepository incidentRepository;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public RelatedSignalsResponse getRelatedSignals(UUID organizationId, UUID projectId, UUID incidentId, int windowMinutes) {
        projectService.getProject(organizationId, projectId);
        Incident incident = incidentRepository.findByIdAndProjectId(incidentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));

        Instant from = incident.getOpenedAt().minus(windowMinutes, ChronoUnit.MINUTES);
        Instant to = incident.getOpenedAt().plus(windowMinutes, ChronoUnit.MINUTES);
        String service = incident.getService();
        Pageable page = PageRequest.of(0, MAX_ITEMS_PER_CATEGORY);

        var logs = ingestionService.searchLogs(projectId, service, null, from, to, page)
                .map(LogEventResponse::from).getContent();
        var errors = ingestionService.searchErrors(projectId, service, null, from, to, page)
                .map(ErrorEventResponse::from).getContent();
        var deployments = ingestionService.searchDeployments(projectId, service, from, to, page)
                .map(DeploymentEventResponse::from).getContent();

        return new RelatedSignalsResponse(logs, errors, deployments);
    }
}
