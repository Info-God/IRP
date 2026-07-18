package com.irp.core.incident;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_timeline_entries")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentTimelineEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    /** User email, "system", or "agent" - kept as free text so later phases don't need a schema change. */
    @Column(nullable = false, length = 150)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private TimelineEntryType entryType;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IncidentTimelineEntry(UUID incidentId, String actor, TimelineEntryType entryType, String message) {
        this.incidentId = incidentId;
        this.actor = actor;
        this.entryType = entryType;
        this.message = message;
    }
}
