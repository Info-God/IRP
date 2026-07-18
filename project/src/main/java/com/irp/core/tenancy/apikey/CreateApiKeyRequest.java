package com.irp.core.tenancy.apikey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiKeyRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name
) {
}
