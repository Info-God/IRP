package com.irp.core.incident.signals.dto;

import com.irp.core.ingestion.dto.DeploymentEventResponse;
import com.irp.core.ingestion.dto.ErrorEventResponse;
import com.irp.core.ingestion.dto.LogEventResponse;

import java.util.List;

public record RelatedSignalsResponse(
        List<LogEventResponse> logs,
        List<ErrorEventResponse> errors,
        List<DeploymentEventResponse> deployments
) {
}
