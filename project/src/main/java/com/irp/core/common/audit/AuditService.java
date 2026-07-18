package com.irp.core.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The single write path into the audit trail. Every other service calls {@link #record}
 * instead of touching {@link AuditLogRepository} directly, so "every AI decision and
 * action is audited" stays true by construction rather than by convention.
 *
 * <p>Deliberately joins the caller's existing transaction (default REQUIRED propagation)
 * rather than opening a new one: the row being audited is often created earlier in that
 * same transaction and hasn't committed yet (e.g. a brand-new Organization), so a
 * REQUIRES_NEW audit write on a separate connection can't see it and trips the audit
 * table's foreign keys. Joining the same transaction also gives the correct semantics
 * for "record what was just committed" - if the surrounding action rolls back, the
 * thing being audited never really happened, so the audit row should roll back with it.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(UUID organizationId, UUID projectId, String actor, String action,
                        String entityType, String entityId, Map<String, Object> metadata) {
        auditLogRepository.save(new AuditLog(organizationId, projectId, actor, action, entityType, entityId, metadata));
    }

    @Transactional
    public void record(UUID organizationId, String actor, String action, String entityType, String entityId) {
        record(organizationId, null, actor, action, entityType, entityId, Map.of());
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> list(UUID organizationId, Optional<UUID> projectId, Pageable pageable) {
        return projectId
                .map(id -> auditLogRepository.findByProjectIdOrderByCreatedAtDesc(id, pageable))
                .orElseGet(() -> auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable));
    }
}
