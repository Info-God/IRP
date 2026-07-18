package com.irp.core.ingestion;

import com.irp.core.ingestion.dto.*;
import com.irp.core.security.apikey.ApiKeyPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Called by the Java SDK, authenticated via the X-API-Key header (see
 * {@link com.irp.core.security.apikey.ApiKeyAuthenticationFilter}) - never by the dashboard.
 */
@RestController
@RequestMapping("/api/v1/ingest")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestionAckResponse ingestLogs(@AuthenticationPrincipal ApiKeyPrincipal apiKeyPrincipal,
                                            @Valid @RequestBody LogEventBatchRequest request) {
        int accepted = ingestionService.ingestLogs(apiKeyPrincipal.projectId(), request.events());
        return new IngestionAckResponse(accepted);
    }

    @PostMapping("/errors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestionAckResponse ingestErrors(@AuthenticationPrincipal ApiKeyPrincipal apiKeyPrincipal,
                                              @Valid @RequestBody ErrorEventBatchRequest request) {
        int accepted = ingestionService.ingestErrors(apiKeyPrincipal.projectId(), request.events());
        return new IngestionAckResponse(accepted);
    }

    @PostMapping("/deployments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestionAckResponse ingestDeployment(@AuthenticationPrincipal ApiKeyPrincipal apiKeyPrincipal,
                                                  @Valid @RequestBody DeploymentEventRequest request) {
        int accepted = ingestionService.ingestDeployment(apiKeyPrincipal.projectId(), request);
        return new IngestionAckResponse(accepted);
    }
}
