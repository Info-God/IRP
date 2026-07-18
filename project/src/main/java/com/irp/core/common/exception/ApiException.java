package com.irp.core.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for exceptions that should be translated into a specific HTTP response
 * by {@link GlobalExceptionHandler}. Prefer the concrete subclasses below over throwing
 * this directly.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
