package com.irp.core.agent.dto;

import com.irp.core.agent.AgentSuggestion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AgentSuggestionResponse(
        UUID id,
        UUID incidentId,
        UUID projectId,
        String rootCause,
        BigDecimal confidence,
        List<Map<String, Object>> evidence,
        List<Map<String, Object>> recommendedActions,
        String status,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt
) {
    public static AgentSuggestionResponse from(AgentSuggestion suggestion) {
        return new AgentSuggestionResponse(
                suggestion.getId(), suggestion.getIncidentId(), suggestion.getProjectId(),
                suggestion.getRootCause(), suggestion.getConfidenceScore(),
                suggestion.getEvidence(), suggestion.getRecommendedActions(),
                suggestion.getStatus().name(), suggestion.getReviewedBy(),
                suggestion.getReviewedAt(), suggestion.getCreatedAt());
    }
}
