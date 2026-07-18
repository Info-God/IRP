package com.irp.core.ingestion;

import java.time.Instant;
import java.util.UUID;

/** A group of error_events sharing (project, stack hash, service, exception type) within a
 * time window - the unit the alert-grouping job decides whether to open an incident for. */
public record ErrorCluster(
        UUID projectId,
        String stackHash,
        String service,
        String exceptionType,
        long count,
        Instant lastOccurredAt
) {
}
