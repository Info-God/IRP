package com.irp.core.incident;

import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.ResourceNotFoundException;
import com.irp.core.incident.dto.CreateIncidentRequest;
import com.irp.core.incident.dto.UpdateIncidentStatusRequest;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineEntryRepository timelineEntryRepository;
    private final ProjectService projectService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Incident createIncident(UUID organizationId, UUID projectId, String actor, CreateIncidentRequest request) {
        projectService.getProject(organizationId, projectId);

        Incident incident = incidentRepository.save(new Incident(
                projectId, request.title(), request.description(),
                IncidentSeverity.valueOf(request.severity()), request.service()));

        timelineEntryRepository.save(new IncidentTimelineEntry(
                incident.getId(), actor, TimelineEntryType.CREATED, "Incident opened: " + incident.getTitle()));

        auditService.record(organizationId, projectId, actor, "INCIDENT_CREATED", "Incident", incident.getId().toString(),
                Map.of("severity", incident.getSeverity().name()));

        eventPublisher.publishEvent(new IncidentCreatedEvent(incident.getId(), projectId, organizationId));

        return incident;
    }

    @Transactional(readOnly = true)
    public Page<Incident> listIncidents(UUID organizationId, UUID projectId, Optional<IncidentStatus> status,
                                         Optional<IncidentSeverity> severity, Pageable pageable) {
        projectService.getProject(organizationId, projectId);

        if (status.isPresent() && severity.isPresent()) {
            return incidentRepository.findByProjectIdAndStatusAndSeverity(projectId, status.get(), severity.get(), pageable);
        }
        if (status.isPresent()) {
            return incidentRepository.findByProjectIdAndStatus(projectId, status.get(), pageable);
        }
        if (severity.isPresent()) {
            return incidentRepository.findByProjectIdAndSeverity(projectId, severity.get(), pageable);
        }
        return incidentRepository.findByProjectId(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public Incident getIncident(UUID organizationId, UUID projectId, UUID incidentId) {
        projectService.getProject(organizationId, projectId);
        return incidentRepository.findByIdAndProjectId(incidentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));
    }

    @Transactional(readOnly = true)
    public List<IncidentTimelineEntry> getTimeline(UUID organizationId, UUID projectId, UUID incidentId) {
        getIncident(organizationId, projectId, incidentId); // validates tenancy + existence
        return timelineEntryRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);
    }

    @Transactional
    public Incident updateStatus(UUID organizationId, UUID projectId, UUID incidentId, String actor,
                                  UpdateIncidentStatusRequest request) {
        Incident incident = getIncident(organizationId, projectId, incidentId);
        IncidentStatus newStatus = IncidentStatus.valueOf(request.status());

        incident.changeStatus(newStatus);
        incidentRepository.save(incident);

        String message = request.note() != null && !request.note().isBlank()
                ? "Status changed to %s: %s".formatted(newStatus, request.note())
                : "Status changed to %s".formatted(newStatus);
        timelineEntryRepository.save(new IncidentTimelineEntry(incident.getId(), actor, TimelineEntryType.STATUS_CHANGE, message));

        auditService.record(organizationId, projectId, actor, "INCIDENT_STATUS_CHANGED", "Incident", incident.getId().toString(),
                Map.of("status", newStatus.name()));

        return incident;
    }
}
