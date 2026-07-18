package com.irp.core.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateIncidentStatusRequest(

        @NotBlank(message = "status is required")
        @Pattern(regexp = "OPEN|INVESTIGATING|AWAITING_APPROVAL|RESOLVED|CLOSED",
                message = "status must be one of OPEN, INVESTIGATING, AWAITING_APPROVAL, RESOLVED, CLOSED")
        String status,

        String note
) {
}
