package com.irp.core.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for both "does not exist" and "exists but belongs to a different tenant" -
 * the two cases are deliberately indistinguishable to a caller.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String entityType, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                "%s not found: %s".formatted(entityType, id));
    }
}
