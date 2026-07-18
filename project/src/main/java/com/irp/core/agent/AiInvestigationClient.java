package com.irp.core.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Calls irp-ai-service's POST /v1/investigations. Failures are logged and swallowed - a down
 * or unreachable AI service must never fail or delay the incident-creation request that
 * triggered it (see {@link AgentInvocationListener}, which calls this asynchronously and
 * only after the incident's own transaction has already committed).
 */
@Component
public class AiInvestigationClient {

    private static final Logger log = LoggerFactory.getLogger(AiInvestigationClient.class);

    private final RestClient restClient;
    private final AiServiceProperties properties;

    public AiInvestigationClient(RestClient.Builder restClientBuilder, AiServiceProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    public void triggerInvestigation(UUID incidentId, UUID projectId, UUID organizationId) {
        try {
            restClient.post()
                    .uri("/v1/investigations")
                    .header("X-Internal-Token", properties.internalToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new InvestigationRequest(incidentId.toString(), projectId.toString(), organizationId.toString()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to auto-trigger AI investigation for incident {}: {}", incidentId, e.getMessage());
        }
    }

    private record InvestigationRequest(
            @JsonProperty("incident_id") String incidentId,
            @JsonProperty("project_id") String projectId,
            @JsonProperty("organization_id") String organizationId
    ) {
    }
}
