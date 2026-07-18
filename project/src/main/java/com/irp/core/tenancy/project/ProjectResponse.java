package com.irp.core.tenancy.project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID organizationId,
        String name,
        String environment,
        Instant createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getOrganizationId(), project.getName(),
                project.getEnvironment(), project.getCreatedAt());
    }
}
