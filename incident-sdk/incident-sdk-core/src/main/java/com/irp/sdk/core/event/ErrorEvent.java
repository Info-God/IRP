package com.irp.sdk.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.irp.sdk.core.util.SensitiveDataMasker;
import com.irp.sdk.core.util.StackTraceUtils;

import java.time.Instant;
import java.util.Map;

/**
 * Field names match irp-core's ErrorEventRequest exactly (occurredAt, service,
 * exceptionType, message, stackTrace, metadata).
 */
public record ErrorEvent(
        Instant occurredAt,
        String service,
        String exceptionType,
        String message,
        String stackTrace,
        Map<String, Object> metadata
) implements IncidentEvent {

    @Override
    @JsonIgnore
    public EventType type() {
        return EventType.ERROR;
    }

    public static ErrorEvent from(Throwable throwable, String service, Map<String, Object> metadata) {
        return new ErrorEvent(
                Instant.now(),
                service,
                throwable.getClass().getName(),
                throwable.getMessage(),
                StackTraceUtils.render(throwable),
                SensitiveDataMasker.mask(metadata));
    }
}
