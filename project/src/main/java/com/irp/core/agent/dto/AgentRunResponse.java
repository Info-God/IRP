package com.irp.core.agent.dto;

import com.irp.core.agent.AgentRun;
import com.irp.core.agent.AgentRunStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AgentRunResponse(
        UUID id,
        UUID incidentId,
        UUID projectId,
        AgentRunStatus status,
        String model,
        Map<String, Object> tokenUsage,
        Instant startedAt,
        Instant finishedAt
) {
    public static AgentRunResponse from(AgentRun run) {
        return new AgentRunResponse(run.getId(), run.getIncidentId(), run.getProjectId(), run.getStatus(),
                run.getModel(), run.getTokenUsage(), run.getStartedAt(), run.getFinishedAt());
    }
}
