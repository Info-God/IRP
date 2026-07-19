# irp-core — Spring Boot Backend

Backend for the Agentic AI Incident Response Platform: multi-tenant org/project management,
JWT auth for dashboard users, API-key auth for SDK/agent traffic, ingestion endpoints,
incident CRUD, an append-only audit log, AI agent run/suggestion storage, runbook +
pgvector search, Slack notifications, and a scheduled job that auto-opens incidents from
repeated errors. See the [root README](../README.md) for the full system picture and the
other three modules.

## Run it

```bash
# 1. start Postgres
docker compose up -d

# 2. run the app (Flyway migrates the schema on boot)
./mvnw spring-boot:run
```

The API is then at `http://localhost:8080`. Override any of `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `SERVER_PORT` as environment
variables — see `src/main/resources/application.yml` for defaults.

**`JWT_SECRET` must be at least 32 bytes** (HS256 requirement). Set a real one before
anything but local dev.

Other environment variables worth knowing about (all have working local-dev defaults —
see `application.yml`): `AI_SERVICE_BASE_URL` / `AI_SERVICE_INTERNAL_TOKEN` /
`AI_SERVICE_AUTO_TRIGGER_ENABLED` (where/whether to call irp-ai-service automatically on
incident creation) and `ALERT_GROUPING_ENABLED` / `_THRESHOLD_COUNT` / `_WINDOW_MINUTES`
/ `_POLL_INTERVAL_MS` (the repeated-error auto-incident job, off by default).

## Example requests

```bash
# Register an organization + owner user
curl -s http://localhost:8080/api/v1/auth/register -X POST \
  -H "Content-Type: application/json" \
  -d '{"organizationName":"Acme Corp","fullName":"Ada Lovelace","email":"ada@acme.dev","password":"correct-horse-battery"}'
# -> { "accessToken": "...", "user": { "organizationId": "...", ... } }

# Log back in
curl -s http://localhost:8080/api/v1/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@acme.dev","password":"correct-horse-battery"}'

TOKEN="<accessToken from above>"

# Create a project
curl -s http://localhost:8080/api/v1/projects -X POST \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"checkout-service","environment":"production"}'

PROJECT_ID="<id from above>"

# Issue an API key for the Java SDK to use (plaintext key is shown once, here)
curl -s http://localhost:8080/api/v1/projects/$PROJECT_ID/api-keys -X POST \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"checkout-prod-key"}'

API_KEY="<plaintextKey from above>"

# Ship a batch of logs (this is what the SDK does under the hood)
curl -s http://localhost:8080/api/v1/ingest/logs -X POST \
  -H "X-API-Key: $API_KEY" -H "Content-Type: application/json" \
  -d '{"events":[{"occurredAt":"2026-07-13T10:00:00Z","level":"ERROR","service":"checkout","message":"NullPointerException at CheckoutService.java:88"}]}'

# Create an incident manually (or let it happen on its own: enough repeated errors with the
# same stackHash within a window auto-open one - see AlertGroupingJob, off by default via
# irp.alert-grouping.enabled)
curl -s http://localhost:8080/api/v1/projects/$PROJECT_ID/incidents -X POST \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Checkout throwing NPEs after deploy","severity":"HIGH","service":"checkout"}'

# List incidents
curl -s "http://localhost:8080/api/v1/projects/$PROJECT_ID/incidents?status=OPEN" \
  -H "Authorization: Bearer $TOKEN"

# Audit trail
curl -s "http://localhost:8080/api/v1/audit-logs?projectId=$PROJECT_ID" \
  -H "Authorization: Bearer $TOKEN"
```

## Design notes

- **Two auth mechanisms, one app**: `/api/v1/ingest/**` and `/api/v1/agent/**` are
  API-key-only, everything else is JWT-only, enforced by two `SecurityFilterChain`s
  matched by path (`SecurityConfig`). irp-ai-service authenticates to this API the same
  way the SDK does — as a project-scoped API key — so a leaked agent key can only ever
  read/write telemetry, never reach dashboard-facing routes.
- **Tenancy is explicit, not magic**: every service method takes `organizationId` (from
  the JWT) and checks it against the resource before returning anything, so a
  cross-tenant lookup 404s instead of leaking existence via 403. There's no
  `ThreadLocal` tenant context. This was audited directly (not assumed) in the hardening
  pass - see `TenantIsolationIT` and the fix to `AuditService.list`, which was the one
  place this guarantee had actually broken.
- **Audit writes are direct service calls**, not an AOP aspect — `AuditService.record(...)`
  is called explicitly at the end of each mutating service method, in the same
  transaction as the action it's recording.
- **Domain events, not direct calls, wire cross-cutting behavior together**:
  `IncidentCreatedEvent` (auto-triggers an AI investigation) and
  `AgentSuggestionApprovedEvent` (notifies Slack if configured) are both published by
  their owning service and consumed by an unrelated `@Async @TransactionalEventListener
  (phase = AFTER_COMMIT)` listener - so `IncidentService`/`AgentSuggestionService` have
  no idea the AI service or Slack exist, and a downstream failure (AI service down,
  Slack unreachable) can never fail the request that triggered it.
