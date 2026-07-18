package com.irp.core.ingestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface DeploymentEventRepository extends JpaRepository<DeploymentEvent, UUID> {

    @Query("""
            SELECT d FROM DeploymentEvent d
            WHERE d.projectId = :projectId
              AND d.occurredAt BETWEEN :from AND :to
              AND (:service IS NULL OR d.service = :service)
            ORDER BY d.occurredAt DESC
            """)
    Page<DeploymentEvent> search(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to,
                                  @Param("service") String service, Pageable pageable);
}
