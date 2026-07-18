-- Raw signal tables populated by the ingestion API (Java SDK -> POST /api/v1/ingest/*).

CREATE TABLE log_events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    occurred_at  TIMESTAMPTZ NOT NULL,
    received_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    level        VARCHAR(10)  NOT NULL,
    service      VARCHAR(150) NOT NULL,
    message      TEXT         NOT NULL,
    trace_id     VARCHAR(100),
    metadata     JSONB
);

CREATE INDEX idx_log_events_project_time ON log_events (project_id, occurred_at DESC);
CREATE INDEX idx_log_events_project_level ON log_events (project_id, level);

CREATE TABLE error_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    occurred_at     TIMESTAMPTZ NOT NULL,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    service         VARCHAR(150) NOT NULL,
    exception_type  VARCHAR(255) NOT NULL,
    message         TEXT,
    stack_trace     TEXT,
    stack_hash      VARCHAR(64)  NOT NULL,
    metadata        JSONB
);

CREATE INDEX idx_error_events_project_time ON error_events (project_id, occurred_at DESC);
CREATE INDEX idx_error_events_project_stack_hash ON error_events (project_id, stack_hash);

CREATE TABLE deployment_events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    occurred_at  TIMESTAMPTZ NOT NULL,
    received_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    service      VARCHAR(150) NOT NULL,
    version      VARCHAR(100) NOT NULL,
    status       VARCHAR(30)  NOT NULL,
    metadata     JSONB
);

CREATE INDEX idx_deployment_events_project_time ON deployment_events (project_id, occurred_at DESC);
