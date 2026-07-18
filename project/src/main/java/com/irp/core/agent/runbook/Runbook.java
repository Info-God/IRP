package com.irp.core.agent.runbook;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "runbooks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Runbook {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(nullable = false)
    private int version;

    @Column(name = "uploaded_by", nullable = false, length = 255)
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Runbook(UUID projectId, String title, String uploadedBy) {
        this.projectId = projectId;
        this.title = title;
        this.uploadedBy = uploadedBy;
        this.version = 1;
    }
}
