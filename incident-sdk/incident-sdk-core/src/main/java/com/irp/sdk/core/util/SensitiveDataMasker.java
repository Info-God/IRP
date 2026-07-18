package com.irp.sdk.core.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Applied to every metadata map before an event is queued, so "avoid sending sensitive
 * data by default" holds even when a caller passes something they shouldn't have -
 * this runs centrally in the event factories, not something each call site has to
 * remember to do itself.
 */
public final class SensitiveDataMasker {

    private static final String REDACTED = "***REDACTED***";

    /** Key names (case-insensitive substring match) whose values are always masked. */
    private static final List<String> SENSITIVE_KEY_FRAGMENTS = List.of(
            "password", "passwd", "secret", "token", "apikey", "api_key", "api-key",
            "authorization", "auth", "credential", "ssn", "creditcard", "credit_card", "cvv"
    );

    private SensitiveDataMasker() {
    }

    public static Map<String, Object> mask(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> masked = new LinkedHashMap<>(metadata.size());
        metadata.forEach((key, value) -> masked.put(key, isSensitiveKey(key) ? REDACTED : value));
        return masked;
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEY_FRAGMENTS.stream().anyMatch(lower::contains);
    }
}
