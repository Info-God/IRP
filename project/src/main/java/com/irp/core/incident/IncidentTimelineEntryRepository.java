package com.irp.core.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentTimelineEntryRepository extends JpaRepository<IncidentTimelineEntry, UUID> {

    List<IncidentTimelineEntry> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
