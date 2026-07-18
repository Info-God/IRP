package com.irp.core.ingestion;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "deployment_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeploymentEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(nullable = false, length = 150)
    private String service;

    @Column(nullable = false, length = 100)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeploymentStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public DeploymentEvent(UUID projectId, Instant occurredAt, String service, String version,
                            DeploymentStatus status, Map<String, Object> metadata) {
        this.projectId = projectId;
        this.occurredAt = occurredAt;
        this.service = service;
        this.version = version;
        this.status = status;
        this.metadata = metadata;
    }
}
