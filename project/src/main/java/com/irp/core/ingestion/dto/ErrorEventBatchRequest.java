package com.irp.core.ingestion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ErrorEventBatchRequest(

        @NotEmpty(message = "events must not be empty")
        @Size(max = 500, message = "a single batch may contain at most 500 events")
        @Valid
        List<ErrorEventRequest> events
) {
}
