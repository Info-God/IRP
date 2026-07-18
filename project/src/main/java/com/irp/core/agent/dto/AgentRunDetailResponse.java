package com.irp.core.agent.dto;

import java.util.List;

public record AgentRunDetailResponse(
        AgentRunResponse run,
        List<AgentStepResponse> steps
) {
}
