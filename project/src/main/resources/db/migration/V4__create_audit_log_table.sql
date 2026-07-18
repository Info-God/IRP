-- Append-only audit trail. Every mutating action in the platform writes exactly one row here.
-- No update/delete path is exposed on this table on purpose.

CREATE TABLE audit_logs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    project_id       UUID REFERENCES projects (id) ON DELETE CASCADE,
    actor            VARCHAR(255) NOT NULL,
    action           VARCHAR(100) NOT NULL,
    entity_type      VARCHAR(100) NOT NULL,
    entity_id        VARCHAR(100) NOT NULL,
    metadata         JSONB,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_org_time ON audit_logs (organization_id, created_at DESC);
CREATE INDEX idx_audit_logs_project_time ON audit_logs (project_id, created_at DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
