package com.irp.core.tenancy.apikey;

import java.time.Instant;
import java.util.UUID;

/**
 * Returned exactly once, at creation time - the plaintext key is never retrievable again
 * because the server only ever stores its hash.
 */
public record ApiKeyCreatedResponse(
        UUID id,
        String name,
        String keyPrefix,
        String plaintextKey,
        Instant createdAt
) {
}
