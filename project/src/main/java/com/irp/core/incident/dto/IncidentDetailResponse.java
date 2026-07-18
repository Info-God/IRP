package com.irp.core.incident.dto;

import java.util.List;

public record IncidentDetailResponse(
        IncidentResponse incident,
        List<IncidentTimelineEntryResponse> timeline
) {
}
