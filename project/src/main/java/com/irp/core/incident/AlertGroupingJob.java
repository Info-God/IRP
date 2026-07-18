package com.irp.core.incident;

import com.irp.core.ingestion.ErrorCluster;
import com.irp.core.ingestion.ErrorEventRepository;
import com.irp.core.tenancy.project.Project;
import com.irp.core.tenancy.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Closes the "someone has to notice the pattern and open an incident" gap: periodically groups
 * repeated errors by their already-computed stackHash (see IngestionService.stackHash()) and
 * auto-opens one incident per cluster, instead of requiring a human or SDK caller to do it.
 * Off by default (irp.alert-grouping.enabled=false) so it never surprises the existing
 * manual-incident-creation flow or tests unless explicitly turned on.
 */
@Component
@RequiredArgsConstructor
public class AlertGroupingJob {

    private static final Logger log = LoggerFactory.getLogger(AlertGroupingJob.class);
    private static final List<IncidentStatus> INACTIVE_STATUSES = List.of(IncidentStatus.RESOLVED, IncidentStatus.CLOSED);

    private final ErrorEventRepository errorEventRepository;
    private final IncidentRepository incidentRepository;
    private final ProjectRepository projectRepository;
    private final IncidentService incidentService;
    private final AlertGroupingProperties properties;

    @Scheduled(fixedDelayString = "${irp.alert-grouping.poll-interval-ms:60000}")
    public void run() {
        if (!properties.enabled()) {
            return;
        }
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(properties.windowMinutes()));
        List<ErrorCluster> clusters = errorEventRepository.findClusters(windowStart, properties.thresholdCount());
        for (ErrorCluster cluster : clusters) {
            try {
                handleCluster(cluster);
            } catch (Exception e) {
                log.warn("Failed to process error cluster (project={}, stackHash={}): {}",
                        cluster.projectId(), cluster.stackHash(), e.getMessage());
            }
        }
    }

    private void handleCluster(ErrorCluster cluster) {
        boolean alreadyTracked = incidentRepository.existsByProjectIdAndStackHashAndStatusNotIn(
                cluster.projectId(), cluster.stackHash(), INACTIVE_STATUSES);
        if (alreadyTracked) {
            return;
        }

        Project project = projectRepository.findById(cluster.projectId()).orElse(null);
        if (project == null) {
            return;
        }

        String title = "%s spike in %s".formatted(cluster.exceptionType(), cluster.service());
        String description = "Auto-detected: %d occurrences of %s in the last %d minutes.".formatted(
                cluster.count(), cluster.exceptionType(), properties.windowMinutes());

        Incident incident = incidentService.createFromAlertGroup(project.getOrganizationId(), cluster.projectId(),
                title, description, cluster.service(), cluster.stackHash(), cluster.count());

        log.info("Auto-created incident {} from {} occurrences of {} in project {}",
                incident.getId(), cluster.count(), cluster.exceptionType(), cluster.projectId());
    }
}
