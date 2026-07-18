package com.irp.sdk.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.irp.sdk.core.util.SensitiveDataMasker;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Field names deliberately match irp-core's LogEventRequest exactly (occurredAt, level,
 * service, message, traceId, metadata) so Jackson can serialize this straight into a
 * body the ingestion API already accepts - no backend change needed. See the
 * trackEvent()/health() factories below for why this type also stands in for two of
 * the SDK's four public methods that don't have a dedicated backend endpoint yet.
 */
public record LogEvent(
        Instant occurredAt,
        String service,
        String level,
        String message,
        String traceId,
        Map<String, Object> metadata
) implements IncidentEvent {

    private static final String MARKER_KEY = "__incident_sdk_type__";

    /** Not a real irp-core field - excluded so it never appears in the outgoing JSON. */
    @Override
    @JsonIgnore
    public EventType type() {
        return EventType.LOG;
    }

    public static LogEvent of(String service, String level, String message, String traceId, Map<String, Object> metadata) {
        return new LogEvent(Instant.now(), service, level, message, traceId, SensitiveDataMasker.mask(metadata));
    }

    /** trackEvent() has no dedicated backend endpoint yet - ships as an INFO log with a marker
     *  so a future dashboard (or a future dedicated endpoint) can tell it apart from a real log line. */
    public static LogEvent trackEvent(String name, String service, String traceId, Map<String, Object> metadata) {
        Map<String, Object> tagged = new HashMap<>(metadata == null ? Map.of() : metadata);
        tagged.put(MARKER_KEY, "TRACK");
        return new LogEvent(Instant.now(), service, "INFO", name, traceId, SensitiveDataMasker.mask(tagged));
    }

    /** sendHealthStatus() - same reasoning as trackEvent(). */
    public static LogEvent health(String service, String environment) {
        return new LogEvent(Instant.now(), service, "INFO", "health-check", null,
                Map.of(MARKER_KEY, "HEALTH", "status", "UP", "environment", environment));
    }
}
