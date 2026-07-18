package com.irp.core.tenancy.apikey;

import com.irp.core.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyCreatedResponse createApiKey(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                               @PathVariable UUID projectId,
                                               @Valid @RequestBody CreateApiKeyRequest request) {
        ApiKeyService.Issued issued = apiKeyService.createApiKey(
                currentUser.getOrganizationId(), projectId, currentUser.getUsername(), request);

        return new ApiKeyCreatedResponse(issued.apiKey().getId(), issued.apiKey().getName(),
                issued.apiKey().getKeyPrefix(), issued.plaintextKey(), issued.apiKey().getCreatedAt());
    }

    @GetMapping
    public List<ApiKeyResponse> listApiKeys(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                             @PathVariable UUID projectId) {
        return apiKeyService.listApiKeys(currentUser.getOrganizationId(), projectId).stream()
                .map(ApiKeyResponse::from)
                .toList();
    }

    @DeleteMapping("/{apiKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeApiKey(@AuthenticationPrincipal AuthenticatedUser currentUser,
                              @PathVariable UUID projectId,
                              @PathVariable UUID apiKeyId) {
        apiKeyService.revokeApiKey(currentUser.getOrganizationId(), projectId, apiKeyId, currentUser.getUsername());
    }
}
