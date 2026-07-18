-- Phase 3 (AI agent + RAG) schema. Mirrors the tenancy pattern used everywhere else in this
-- schema: every row carries project_id. agent_suggestions is the only table Java actively
-- reads/writes as of this migration (see AgentSuggestion/AgentSuggestionRepository) - the
-- other four are laid down now so the AI agent service has a stable schema to target next,
-- but no Java code touches them yet.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE runbooks (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id    UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title         VARCHAR(255) NOT NULL,
    storage_path  VARCHAR(500) NOT NULL,
    version       INT NOT NULL DEFAULT 1,
    uploaded_by   VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_runbooks_project ON runbooks (project_id);

CREATE TABLE runbook_chunks (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    runbook_id   UUID NOT NULL REFERENCES runbooks (id) ON DELETE CASCADE,
    project_id   UUID NOT NULL,
    chunk_index  INT NOT NULL,
    content      TEXT NOT NULL,
    embedding    VECTOR(1536) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_runbook_chunks_embedding ON runbook_chunks USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_runbook_chunks_project ON runbook_chunks (project_id);

CREATE TABLE agent_runs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id  UUID NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    project_id   UUID NOT NULL,
    status       VARCHAR(20) NOT NULL,  -- RUNNING, SUCCEEDED, FAILED
    model        VARCHAR(100),
    token_usage  JSONB,
    started_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at  TIMESTAMPTZ
);

CREATE INDEX idx_agent_runs_incident ON agent_runs (incident_id);

CREATE TABLE agent_steps (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_run_id  UUID NOT NULL REFERENCES agent_runs (id) ON DELETE CASCADE,
    step_index    INT NOT NULL,
    tool_name     VARCHAR(100) NOT NULL,
    tool_input    JSONB,
    tool_output   JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_agent_steps_run ON agent_steps (agent_run_id);

CREATE TABLE agent_suggestions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id          UUID NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    project_id           UUID NOT NULL,
    root_cause           TEXT NOT NULL,
    confidence_score     NUMERIC(3,2) NOT NULL CHECK (confidence_score >= 0 AND confidence_score <= 1),
    evidence             JSONB NOT NULL,
    recommended_actions  JSONB NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW',
    reviewed_by          VARCHAR(255),
    reviewed_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_agent_suggestions_incident ON agent_suggestions (incident_id);
CREATE INDEX idx_agent_suggestions_project_status ON agent_suggestions (project_id, status);
