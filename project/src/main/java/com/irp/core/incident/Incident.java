package com.irp.core.incident;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(length = 150)
    private String service;

    /** Set only for incidents auto-created by {@link AlertGroupingJob}; null for manually-created ones. */
    @Column(name = "stack_hash", length = 64)
    private String stackHash;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Incident(UUID projectId, String title, String description, IncidentSeverity severity, String service) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.service = service;
        this.status = IncidentStatus.OPEN;
        this.openedAt = Instant.now();
    }

    public void changeStatus(IncidentStatus newStatus) {
        this.status = newStatus;
        if (newStatus == IncidentStatus.RESOLVED || newStatus == IncidentStatus.CLOSED) {
            this.resolvedAt = Instant.now();
        }
    }
}
