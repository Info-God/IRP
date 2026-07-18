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
@Table(name = "error_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorEvent {

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

    @Column(name = "exception_type", nullable = false, length = 255)
    private String exceptionType;

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "stack_trace", columnDefinition = "text")
    private String stackTrace;

    /** Hash of exception type + top stack frames - used to group repeat occurrences of the same error. */
    @Column(name = "stack_hash", nullable = false, length = 64)
    private String stackHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public ErrorEvent(UUID projectId, Instant occurredAt, String service, String exceptionType,
                       String message, String stackTrace, String stackHash, Map<String, Object> metadata) {
        this.projectId = projectId;
        this.occurredAt = occurredAt;
        this.service = service;
        this.exceptionType = exceptionType;
        this.message = message;
        this.stackTrace = stackTrace;
        this.stackHash = stackHash;
        this.metadata = metadata;
    }
}
