# irp-core — Phase 1: Backend Foundation

Spring Boot backend foundation for the Agentic AI Incident Response Platform. This phase
covers multi-tenant org/project management, JWT auth for dashboard users, API-key auth
for SDK ingestion, the ingestion endpoints, incident CRUD, and an append-only audit log.
No AI, frontend, RabbitMQ, or cloud deployment yet — those are later phases.

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

# Create an incident manually (later phases wire this up automatically from ingested errors)
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

## Design notes for later phases

- **Two auth mechanisms, one app**: `/api/v1/ingest/**` is API-key-only, everything else
  is JWT-only, enforced by two `SecurityFilterChain`s matched by path
  (`SecurityConfig`). The AI agent service (Phase 3+) should authenticate to the core
  platform the same way the SDK does — as a project-scoped API key.
- **Tenancy is explicit, not magic**: every service method takes `organizationId` (from
  the JWT) and checks it against the resource before returning anything, so a
  cross-tenant lookup 404s instead of leaking existence via 403. There's no
  `ThreadLocal` tenant context — keep it that way unless a real cross-cutting need
  shows up.
- **Audit writes are direct service calls**, not an AOP aspect — `AuditService.record(...)`
  is called explicitly at the end of each mutating service method. When Phase 2 adds
  RabbitMQ, the natural evolution is a single consumer on every topic writing to
  `audit_logs`, which will make the direct calls in Java services redundant for
  event-sourced actions (but ingestion/incident CRUD here has no event bus yet).
- **`IncidentTimelineEntry`** and **`Incident.status`** already anticipate the agent:
  `TimelineEntryType.AGENT_ACTION` and `IncidentStatus.AWAITING_APPROVAL` exist now so
  Phase 3 doesn't need a migration just to let the agent post to the timeline.
