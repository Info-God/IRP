package com.irp.core.integrations.slack;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SlackIntegrationRepository extends JpaRepository<SlackIntegration, UUID> {

    Optional<SlackIntegration> findByProjectId(UUID projectId);
}
