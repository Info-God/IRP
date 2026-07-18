-- Incidents and their timeline. In Phase 1 incidents are created directly via the API;
-- later phases add a rule-based detector and an AI agent that write to the same tables.

CREATE TABLE incidents (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    severity     VARCHAR(20) NOT NULL,
    status       VARCHAR(30) NOT NULL,
    service      VARCHAR(150),
    opened_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_project_status ON incidents (project_id, status);
CREATE INDEX idx_incidents_project_opened ON incidents (project_id, opened_at DESC);

CREATE TABLE incident_timeline_entries (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id  UUID NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    actor        VARCHAR(150) NOT NULL,
    entry_type   VARCHAR(30)  NOT NULL,
    message      TEXT         NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_timeline_incident_id ON incident_timeline_entries (incident_id, created_at);
