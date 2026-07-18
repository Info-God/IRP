package com.irp.core.incident;

import com.irp.core.common.web.PageResponse;
import com.irp.core.incident.dto.*;
import com.irp.core.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse createIncident(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                            @PathVariable UUID projectId,
                                            @Valid @RequestBody CreateIncidentRequest request) {
        Incident incident = incidentService.createIncident(
                currentUser.getOrganizationId(), projectId, currentUser.getUsername(), request);
        return IncidentResponse.from(incident);
    }

    @GetMapping
    public PageResponse<IncidentResponse> listIncidents(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                          @PathVariable UUID projectId,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) String severity,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        Optional<IncidentStatus> statusFilter = Optional.ofNullable(status).map(IncidentStatus::valueOf);
        Optional<IncidentSeverity> severityFilter = Optional.ofNullable(severity).map(IncidentSeverity::valueOf);

        return PageResponse.from(incidentService
                .listIncidents(currentUser.getOrganizationId(), projectId, statusFilter, severityFilter, pageable)
                .map(IncidentResponse::from));
    }

    @GetMapping("/{incidentId}")
    public IncidentDetailResponse getIncident(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                               @PathVariable UUID projectId,
                                               @PathVariable UUID incidentId) {
        Incident incident = incidentService.getIncident(currentUser.getOrganizationId(), projectId, incidentId);
        var timeline = incidentService.getTimeline(currentUser.getOrganizationId(), projectId, incidentId).stream()
                .map(IncidentTimelineEntryResponse::from)
                .toList();
        return new IncidentDetailResponse(IncidentResponse.from(incident), timeline);
    }

    @PatchMapping("/{incidentId}/status")
    public IncidentResponse updateStatus(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                          @PathVariable UUID projectId,
                                          @PathVariable UUID incidentId,
                                          @Valid @RequestBody UpdateIncidentStatusRequest request) {
        Incident incident = incidentService.updateStatus(
                currentUser.getOrganizationId(), projectId, incidentId, currentUser.getUsername(), request);
        return IncidentResponse.from(incident);
    }
}
