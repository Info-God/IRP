package com.irp.core.security.apikey;

import java.util.UUID;

/**
 * Authenticated identity for SDK ingestion requests - a project-scoped API key,
 * not a human user. Controllers pull this out of the SecurityContext to know which
 * project incoming events belong to.
 */
public record ApiKeyPrincipal(
        UUID apiKeyId,
        UUID projectId,
        UUID organizationId,
        String keyPrefix
) {
}
