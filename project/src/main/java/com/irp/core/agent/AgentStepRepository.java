package com.irp.core.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentStepRepository extends JpaRepository<AgentStep, UUID> {

    List<AgentStep> findByAgentRunIdOrderByStepIndexAsc(UUID agentRunId);

    int countByAgentRunId(UUID agentRunId);
}
