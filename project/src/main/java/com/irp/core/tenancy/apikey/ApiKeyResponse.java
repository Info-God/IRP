package com.irp.core.tenancy.apikey;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String name,
        String keyPrefix,
        Instant lastUsedAt,
        Instant revokedAt,
        Instant createdAt
) {
    public static ApiKeyResponse from(ApiKey key) {
        return new ApiKeyResponse(key.getId(), key.getName(), key.getKeyPrefix(),
                key.getLastUsedAt(), key.getRevokedAt(), key.getCreatedAt());
    }
}
