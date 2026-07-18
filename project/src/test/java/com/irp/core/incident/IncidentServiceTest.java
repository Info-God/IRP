package com.irp.core.incident;

import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.ResourceNotFoundException;
import com.irp.core.incident.dto.CreateIncidentRequest;
import com.irp.core.tenancy.project.Project;
import com.irp.core.tenancy.project.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit test: repositories/services are mocked, so this exercises IncidentService's
 * own logic (tenancy checks, status transitions, timeline writes) without a database.
 * See IrpCoreApplicationIT for the Testcontainers-backed end-to-end equivalent.
 */
@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private IncidentTimelineEntryRepository timelineEntryRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private AuditService auditService;

    private IncidentService incidentService;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @Test
    void createIncidentPersistsIncidentAndOpeningTimelineEntry() {
        incidentService = new IncidentService(incidentRepository, timelineEntryRepository, projectService, auditService);

        when(projectService.getProject(organizationId, projectId)).thenReturn(newProject());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(this::simulateGeneratedId);

        CreateIncidentRequest request = new CreateIncidentRequest("Checkout down", "NPE spike", "HIGH", "checkout");

        Incident created = incidentService.createIncident(organizationId, projectId, "ada@acme.dev", request);

        assertThat(created.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(created.getSeverity()).isEqualTo(IncidentSeverity.HIGH);
        verify(timelineEntryRepository).save(argThat(entry ->
                entry.getEntryType() == TimelineEntryType.CREATED && entry.getActor().equals("ada@acme.dev")));
        verify(auditService).record(eq(organizationId), eq(projectId), eq("ada@acme.dev"),
                eq("INCIDENT_CREATED"), eq("Incident"), any(), anyMap());
    }

    @Test
    void createIncidentRejectsProjectFromAnotherOrganization() {
        incidentService = new IncidentService(incidentRepository, timelineEntryRepository, projectService, auditService);

        when(projectService.getProject(organizationId, projectId))
                .thenThrow(new ResourceNotFoundException("Project", projectId));

        CreateIncidentRequest request = new CreateIncidentRequest("title", null, "LOW", null);

        assertThatThrownBy(() -> incidentService.createIncident(organizationId, projectId, "ada@acme.dev", request))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(incidentRepository);
    }

    @Test
    void updateStatusToResolvedStampsResolvedAt() {
        incidentService = new IncidentService(incidentRepository, timelineEntryRepository, projectService, auditService);

        Incident incident = newIncident();
        incident.setId(UUID.randomUUID());
        when(projectService.getProject(organizationId, projectId)).thenReturn(newProject());
        when(incidentRepository.findByIdAndProjectId(incident.getId(), projectId)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new com.irp.core.incident.dto.UpdateIncidentStatusRequest("RESOLVED", "rolled back the bad deploy");

        Incident updated = incidentService.updateStatus(organizationId, projectId, incident.getId(), "ada@acme.dev", request);

        assertThat(updated.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(updated.getResolvedAt()).isNotNull();
    }

    /** Mimics what Hibernate does on save() for a @GeneratedValue id, since these entities are never actually persisted here. */
    private Incident simulateGeneratedId(org.mockito.invocation.InvocationOnMock invocation) {
        Incident incident = invocation.getArgument(0);
        incident.setId(UUID.randomUUID());
        return incident;
    }

    private Project newProject() {
        return new Project(organizationId, "checkout-service", "production");
    }

    private Incident newIncident() {
        return new Incident(projectId, "Checkout down", "desc", IncidentSeverity.HIGH, "checkout");
    }
}
