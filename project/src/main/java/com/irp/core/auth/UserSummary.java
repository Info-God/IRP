package com.irp.core.auth;

import com.irp.core.tenancy.user.AppUser;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String email,
        String fullName,
        String role,
        UUID organizationId
) {
    public static UserSummary from(AppUser user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getOrganizationId());
    }
}
