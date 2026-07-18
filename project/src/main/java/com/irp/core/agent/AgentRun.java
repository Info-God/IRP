package com.irp.core.agent;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "agent_runs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentRun {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentRunStatus status;

    @Column(length = 100)
    private String model;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "token_usage", columnDefinition = "jsonb")
    private Map<String, Object> tokenUsage;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    public AgentRun(UUID incidentId, UUID projectId, String model) {
        this.incidentId = incidentId;
        this.projectId = projectId;
        this.model = model;
        this.status = AgentRunStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void finish(AgentRunStatus status, Map<String, Object> tokenUsage) {
        this.status = status;
        this.tokenUsage = tokenUsage;
        this.finishedAt = Instant.now();
    }
}
