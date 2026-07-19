package com.irp.core.integrations.slack;

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
@Table(name = "slack_integrations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackIntegration {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    /** Plaintext, deliberately not surfaced back to callers - see SlackIntegrationResponse's
     * masking. A leaked incoming-webhook URL lets anyone post to the connected channel. */
    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "connected_by", nullable = false, length = 255)
    private String connectedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SlackIntegration(UUID projectId, String webhookUrl, String connectedBy) {
        this.projectId = projectId;
        this.webhookUrl = webhookUrl;
        this.connectedBy = connectedBy;
    }

    public void reconnect(String webhookUrl, String connectedBy) {
        this.webhookUrl = webhookUrl;
        this.connectedBy = connectedBy;
    }
}
