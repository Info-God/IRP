# incident-sdk — Phase 2: Java SDK & Spring Boot Starter

Three-module Maven reactor:

- **`incident-sdk-core`** — plain Java 21, zero Spring dependency. `IncidentClient`, the
  event model, async dispatch, HTTP transport, retry.
- **`incident-spring-boot-starter`** — auto-configures an `IncidentClient` bean plus
  automatic request-latency and uncaught-exception capture for any Spring Boot 3 app.
- **`incident-sdk-demo-app`** — a throwaway Spring Boot app that depends on the starter,
  used to prove the whole thing works end to end against a running `irp-core` (Phase 1).

## Build

```bash
cd incident-sdk
./mvnw clean install
```

This compiles all three modules and runs every unit test, including `HttpSenderTest`,
which spins up a real (tiny) HTTP server via the JDK's built-in `com.sun.net.httpserver.HttpServer`
to verify the exact JSON shape and headers sent match what `irp-core`'s ingestion API expects
— no mocking library needed for that.

## Run the demo app against a live irp-core

1. **Start irp-core** (Phase 1, in the sibling `project/` directory):
   ```bash
   cd ../project
   docker compose up -d
   ./mvnw spring-boot:run
   ```

2. **Get a real API key** — register, create a project, and issue a key (see `project/README.md`
   for the exact curl commands), or do it via Postman. Copy the `plaintextKey` value.

3. **Run the demo app** with that key:
   ```bash
   cd incident-sdk/incident-sdk-demo-app
   INCIDENT_API_KEY=irp_live_xxxxx ../mvnw spring-boot:run
   ```
   (or set `INCIDENT_API_KEY` as an environment variable however your shell prefers).

4. **Exercise every method:**
   ```bash
   curl http://localhost:9090/demo/track     # trackEvent()
   curl http://localhost:9090/demo/error     # captureException() - manual
   curl http://localhost:9090/demo/deploy    # markDeployment()
   curl http://localhost:9090/demo/health    # sendHealthStatus()
   curl http://localhost:9090/demo/boom      # uncaught exception - AUTOMATIC capture
   curl http://localhost:9090/demo/slow      # slow request - AUTOMATIC latency capture
   ```

5. **Verify it actually landed in irp-core**, using the JWT from step 2:
   ```bash
   curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/v1/projects/$PROJECT_ID/incidents"
   # or check the audit log / query the log_events / error_events / deployment_events
   # tables directly in Postgres
   ```

6. **Check the SDK's own health indicator**, which is separate from what it reports to
   the platform:
   ```bash
   curl http://localhost:9090/actuator/health
   ```
   Look for an `"incident"` component in the response.

## Configuration reference

```yaml
incident:
  enabled: true                                    # default true - kill switch
  api-key: irp_live_xxxxx                          # required
  endpoint: http://localhost:8080/api/v1/ingest     # required
  service-name: payment-service                     # required
  environment: dev                                  # default "production"
  queue:
    capacity: 2000        # default
    batch-size: 50        # default
    flush-interval: 2s    # default
  retry:
    max-attempts: 3        # default
    initial-backoff: 200ms # default
    max-backoff: 5s        # default
  latency-capture:
    enabled: true    # default
    slow-threshold: 0ms   # default (0 = report every request)
  error-capture:
    enabled: true    # default
  health:
    auto-report:
      enabled: false   # default off
      interval: 60s    # default
```

## Do NOT do this in production

`docker-compose.yml` and the demo app's `application.yml` use `INCIDENT_API_KEY:changeme`
as a fallback default purely so the app doesn't fail to *start* without one set - the SDK
itself will just silently fail every send with a 401 (logged once, not retried) if that
placeholder is actually used. Always set a real key via environment variable or a secrets
manager, never commit one to `application.yml`.
