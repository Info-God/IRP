package com.irp.core.common.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID projectId,
        String actor,
        String action,
        String entityType,
        String entityId,
        Map<String, Object> metadata,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getProjectId(), log.getActor(), log.getAction(),
                log.getEntityType(), log.getEntityId(), log.getMetadata(), log.getCreatedAt());
    }
}
