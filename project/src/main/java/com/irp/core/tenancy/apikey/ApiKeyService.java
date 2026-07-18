package com.irp.core.tenancy.apikey;

import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.ResourceNotFoundException;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ProjectService projectService;
    private final ApiKeyHasher apiKeyHasher;
    private final AuditService auditService;

    /** Holds the plaintext key alongside the persisted entity - the only point in the system where it exists. */
    public record Issued(ApiKey apiKey, String plaintextKey) {
    }

    @Transactional
    public Issued createApiKey(UUID organizationId, UUID projectId, String actor, CreateApiKeyRequest request) {
        projectService.getProject(organizationId, projectId); // throws 404 if the project isn't in this org

        String plaintextKey = apiKeyHasher.generate();
        String keyHash = apiKeyHasher.hash(plaintextKey);
        String displayPrefix = apiKeyHasher.displayPrefix(plaintextKey);

        ApiKey apiKey = apiKeyRepository.save(new ApiKey(projectId, request.name(), displayPrefix, keyHash));

        auditService.record(organizationId, projectId, actor, "API_KEY_CREATED", "ApiKey", apiKey.getId().toString(),
                Map.of("name", apiKey.getName(), "keyPrefix", displayPrefix));

        return new Issued(apiKey, plaintextKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> listApiKeys(UUID organizationId, UUID projectId) {
        projectService.getProject(organizationId, projectId);
        return apiKeyRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    @Transactional
    public void revokeApiKey(UUID organizationId, UUID projectId, UUID apiKeyId, String actor) {
        projectService.getProject(organizationId, projectId);

        ApiKey apiKey = apiKeyRepository.findByIdAndProjectId(apiKeyId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", apiKeyId));

        apiKey.setRevokedAt(Instant.now());
        apiKeyRepository.save(apiKey);

        auditService.record(organizationId, projectId, actor, "API_KEY_REVOKED", "ApiKey", apiKey.getId().toString(), Map.of());
    }
}
