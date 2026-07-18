package com.irp.core.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Bootstraps a brand-new tenant: creates the Organization and its first user as OWNER.
 * Additional users are added later via an invite flow (out of scope for Phase 1).
 */
public record RegisterRequest(

        @NotBlank(message = "organizationName is required")
        @Size(max = 150)
        String organizationName,

        @NotBlank(message = "fullName is required")
        @Size(max = 150)
        String fullName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be at least 8 characters")
        String password
) {
}
