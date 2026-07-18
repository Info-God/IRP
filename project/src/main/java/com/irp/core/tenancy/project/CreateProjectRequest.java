package com.irp.core.tenancy.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "environment is required")
        @Pattern(regexp = "development|staging|production", message = "environment must be one of: development, staging, production")
        String environment
) {
}
