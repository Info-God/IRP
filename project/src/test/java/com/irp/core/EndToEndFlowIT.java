package com.irp.core;

import com.irp.core.auth.AuthResponse;
import com.irp.core.auth.RegisterRequest;
import com.irp.core.incident.dto.CreateIncidentRequest;
import com.irp.core.incident.dto.IncidentResponse;
import com.irp.core.ingestion.dto.IngestionAckResponse;
import com.irp.core.ingestion.dto.LogEventBatchRequest;
import com.irp.core.ingestion.dto.LogEventRequest;
import com.irp.core.support.AbstractIntegrationTest;
import com.irp.core.tenancy.apikey.ApiKeyCreatedResponse;
import com.irp.core.tenancy.apikey.CreateApiKeyRequest;
import com.irp.core.tenancy.project.CreateProjectRequest;
import com.irp.core.tenancy.project.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Walks the whole Phase 1 golden path against a real Postgres container: register an
 * org, create a project, issue an API key, ingest a log through it, then open and list
 * an incident as the dashboard user. If any layer's wiring is wrong (security chains,
 * Flyway schema, JSON mapping) this is what catches it.
 */
class EndToEndFlowIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerIngestAndTrackAnIncidentEndToEnd() {
        // 1. register -> get a JWT
        var registerRequest = new RegisterRequest("Acme Corp", "Ada Lovelace", "ada+" + Instant.now().toEpochMilli() + "@acme.dev", "correct-horse-battery");
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                "/api/v1/auth/register", registerRequest, AuthResponse.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String jwt = registerResponse.getBody().accessToken();

        HttpHeaders jwtHeaders = new HttpHeaders();
        jwtHeaders.setBearerAuth(jwt);

        // 2. create a project
        var createProject = new HttpEntity<>(new CreateProjectRequest("checkout-service", "production"), jwtHeaders);
        ResponseEntity<ProjectResponse> projectResponse = restTemplate.postForEntity(
                "/api/v1/projects", createProject, ProjectResponse.class);
        assertThat(projectResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var projectId = projectResponse.getBody().id();

        // 3. issue an API key for that project
        var createKey = new HttpEntity<>(new CreateApiKeyRequest("ci-key"), jwtHeaders);
        ResponseEntity<ApiKeyCreatedResponse> keyResponse = restTemplate.postForEntity(
                "/api/v1/projects/" + projectId + "/api-keys", createKey, ApiKeyCreatedResponse.class);
        assertThat(keyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String plaintextKey = keyResponse.getBody().plaintextKey();

        // 4. ingest a log using ONLY the API key (no JWT)
        HttpHeaders apiKeyHeaders = new HttpHeaders();
        apiKeyHeaders.set("X-API-Key", plaintextKey);
        var logBatch = new LogEventBatchRequest(List.of(
                new LogEventRequest(Instant.now(), "ERROR", "checkout", "NPE at CheckoutService.java:88", null, Map.of())));
        ResponseEntity<IngestionAckResponse> ingestResponse = restTemplate.postForEntity(
                "/api/v1/ingest/logs", new HttpEntity<>(logBatch, apiKeyHeaders), IngestionAckResponse.class);
        assertThat(ingestResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(ingestResponse.getBody().accepted()).isEqualTo(1);

        // 5. the API key must NOT work against a JWT-only route
        ResponseEntity<String> apiKeyOnDashboardRoute = restTemplate.exchange(
                "/api/v1/projects", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(apiKeyHeaders), String.class);
        assertThat(apiKeyOnDashboardRoute.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // 6. open an incident as the dashboard user
        var createIncident = new HttpEntity<>(
                new CreateIncidentRequest("Checkout throwing NPEs", "seen right after deploy", "HIGH", "checkout"), jwtHeaders);
        ResponseEntity<IncidentResponse> incidentResponse = restTemplate.postForEntity(
                "/api/v1/projects/" + projectId + "/incidents", createIncident, IncidentResponse.class);
        assertThat(incidentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(incidentResponse.getBody().status()).isEqualTo("OPEN");

        // 7. list incidents for the project and find it
        ResponseEntity<Map> listResponse = restTemplate.exchange(
                "/api/v1/projects/" + projectId + "/incidents?status=OPEN", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(jwtHeaders), Map.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Integer) listResponse.getBody().get("totalElements")).isEqualTo(1);
    }
}
