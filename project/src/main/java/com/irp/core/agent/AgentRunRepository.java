package com.irp.core.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRunRepository extends JpaRepository<AgentRun, UUID> {

    Optional<AgentRun> findByIdAndProjectId(UUID id, UUID projectId);

    List<AgentRun> findByIncidentIdOrderByStartedAtDesc(UUID incidentId);
}
