package com.irp.core.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentSuggestionRepository extends JpaRepository<AgentSuggestion, UUID> {

    Optional<AgentSuggestion> findByIdAndProjectId(UUID id, UUID projectId);

    List<AgentSuggestion> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
}
