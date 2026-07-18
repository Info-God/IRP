package com.irp.core.agent;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "agent_suggestions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentSuggestion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "root_cause", nullable = false, columnDefinition = "text")
    private String rootCause;

    @Column(name = "confidence_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal confidenceScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> evidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommended_actions", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> recommendedActions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentSuggestionStatus status;

    @Column(name = "reviewed_by", length = 255)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AgentSuggestion(UUID incidentId, UUID projectId, String rootCause, BigDecimal confidenceScore,
                            List<Map<String, Object>> evidence, List<Map<String, Object>> recommendedActions) {
        this.incidentId = incidentId;
        this.projectId = projectId;
        this.rootCause = rootCause;
        this.confidenceScore = confidenceScore;
        this.evidence = evidence;
        this.recommendedActions = recommendedActions;
        this.status = AgentSuggestionStatus.PENDING_REVIEW;
    }

    public void review(AgentSuggestionStatus decision, String reviewer) {
        this.status = decision;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
    }
}
