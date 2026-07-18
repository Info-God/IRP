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
@Table(name = "log_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogEvent {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LogLevel level;

    @Column(nullable = false, length = 150)
    private String service;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public LogEvent(UUID projectId, Instant occurredAt, LogLevel level, String service,
                     String message, String traceId, Map<String, Object> metadata) {
        this.projectId = projectId;
        this.occurredAt = occurredAt;
        this.level = level;
        this.service = service;
        this.message = message;
        this.traceId = traceId;
        this.metadata = metadata;
    }
}
