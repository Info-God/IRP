package com.irp.core.common.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Uniform error body for every non-2xx response the API returns.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, path, List.of());
    }

    public static ErrorResponse ofFieldErrors(int status, String message, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, "VALIDATION_FAILED", message, path, fieldErrors);
    }

    public static ErrorResponse ofFieldErrorMap(int status, String message, String path, Map<String, String> fieldErrors) {
        return ofFieldErrors(status, message, path,
                fieldErrors.entrySet().stream()
                        .map(e -> new FieldError(e.getKey(), e.getValue()))
                        .toList());
    }
}
