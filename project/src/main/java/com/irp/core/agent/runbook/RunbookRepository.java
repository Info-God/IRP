package com.irp.core.agent.runbook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunbookRepository extends JpaRepository<Runbook, UUID> {

    List<Runbook> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<Runbook> findByIdAndProjectId(UUID id, UUID projectId);
}
