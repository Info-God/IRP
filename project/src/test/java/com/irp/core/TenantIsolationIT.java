package com.irp.core;

import com.irp.core.auth.AuthResponse;
import com.irp.core.auth.RegisterRequest;
import com.irp.core.incident.dto.CreateIncidentRequest;
import com.irp.core.incident.dto.IncidentResponse;
import com.irp.core.support.AbstractIntegrationTest;
import com.irp.core.tenancy.project.CreateProjectRequest;
import com.irp.core.tenancy.project.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the report's "a cross-tenant lookup returns 404, revealing nothing"
 * claim - two completely separate organizations are registered against a real Postgres
 * container, and org B is checked against every project-scoped read org A owns. Also
 * covers the one place this claim turned out to be false: AuditService.list's projectId
 * branch, which queried by projectId alone with no organizationId check until this phase.
 */
class TenantIsolationIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void crossTenantIncidentAndSuggestionLookupsReturn404NotData() {
        HttpHeaders headersA = registerAndGetAuthHeaders("orgA-tenancy");
        UUID projectIdA = createProject(headersA, "checkout-service");
        UUID incidentIdA = createIncident(headersA, projectIdA, "Checkout throwing NPEs");

        HttpHeaders headersB = registerAndGetAuthHeaders("orgB-tenancy");

        assertThat(get(headersB, "/api/v1/projects/" + projectIdA + "/incidents/" + incidentIdA))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(headersB, "/api/v1/projects/" + projectIdA + "/incidents"))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(headersB, "/api/v1/projects/" + projectIdA + "/incidents/" + incidentIdA + "/agent-suggestions"))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(headersB, "/api/v1/projects/" + projectIdA + "/incidents/" + incidentIdA + "/agent-runs"))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(headersB, "/api/v1/projects/" + projectIdA + "/runbooks"))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(headersB, "/api/v1/projects/" + projectIdA + "/api-keys"))
                .isEqualTo(HttpStatus.NOT_FOUND);

        // Sanity check: org A can read its own incident fine, so the 404s above are really
        // about tenancy and not e.g. a typo'd URL that 404s for everyone.
        assertThat(get(headersA, "/api/v1/projects/" + projectIdA + "/incidents/" + incidentIdA))
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void crossTenantAuditLogQueryByProjectIdReturnsNoRows() {
        HttpHeaders headersA = registerAndGetAuthHeaders("orgA-audit");
        UUID projectIdA = createProject(headersA, "payments-service"); // itself writes a PROJECT_CREATED audit row

        HttpHeaders headersB = registerAndGetAuthHeaders("orgB-audit");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/audit-logs?projectId=" + projectIdA, HttpMethod.GET,
                new HttpEntity<>(headersB), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Integer) response.getBody().get("totalElements")).isEqualTo(0);

        // Sanity check: org A can see its own audit trail for that project.
        ResponseEntity<Map> ownResponse = restTemplate.exchange(
                "/api/v1/audit-logs?projectId=" + projectIdA, HttpMethod.GET,
                new HttpEntity<>(headersA), Map.class);
        assertThat((Integer) ownResponse.getBody().get("totalElements")).isGreaterThan(0);
    }

    private HttpStatus get(HttpHeaders headers, String path) {
        ResponseEntity<String> response = restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return (HttpStatus) response.getStatusCode();
    }

    private HttpHeaders registerAndGetAuthHeaders(String orgPrefix) {
        var request = new RegisterRequest(orgPrefix, "Test User",
                orgPrefix + "+" + Instant.now().toEpochMilli() + "@acme.dev", "correct-horse-battery");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/v1/auth/register", request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(response.getBody().accessToken());
        return headers;
    }

    private UUID createProject(HttpHeaders headers, String name) {
        var request = new CreateProjectRequest(name, "production");
        ResponseEntity<ProjectResponse> response = restTemplate.postForEntity(
                "/api/v1/projects", new HttpEntity<>(request, headers), ProjectResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    private UUID createIncident(HttpHeaders headers, UUID projectId, String title) {
        var request = new CreateIncidentRequest(title, "test description", "HIGH", "checkout");
        ResponseEntity<IncidentResponse> response = restTemplate.postForEntity(
                "/api/v1/projects/" + projectId + "/incidents", new HttpEntity<>(request, headers), IncidentResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }
}
