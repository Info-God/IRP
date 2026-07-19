-- One Slack incoming-webhook config per project (Phase 8: the one real integration,
-- replacing the Integrations page's fully-mocked catalog for exactly this one card).
-- PagerDuty/GitHub/generic-webhook stay UI-only "coming soon" - no tables for those yet.

CREATE TABLE slack_integrations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id    UUID NOT NULL UNIQUE REFERENCES projects (id) ON DELETE CASCADE,
    webhook_url   VARCHAR(500) NOT NULL,
    connected_by  VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
