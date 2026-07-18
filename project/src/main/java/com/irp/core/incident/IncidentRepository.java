package com.irp.core.incident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Optional<Incident> findByIdAndProjectId(UUID id, UUID projectId);

    boolean existsByProjectIdAndStackHashAndStatusNotIn(UUID projectId, String stackHash, List<IncidentStatus> excludedStatuses);

    Page<Incident> findByProjectId(UUID projectId, Pageable pageable);

    Page<Incident> findByProjectIdAndStatus(UUID projectId, IncidentStatus status, Pageable pageable);

    Page<Incident> findByProjectIdAndSeverity(UUID projectId, IncidentSeverity severity, Pageable pageable);

    Page<Incident> findByProjectIdAndStatusAndSeverity(
            UUID projectId, IncidentStatus status, IncidentSeverity severity, Pageable pageable);
}
