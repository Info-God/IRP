package com.irp.core.ingestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ErrorEventRepository extends JpaRepository<ErrorEvent, UUID> {

    @Query("""
            SELECT e FROM ErrorEvent e
            WHERE e.projectId = :projectId
              AND e.occurredAt BETWEEN :from AND :to
              AND (:service IS NULL OR e.service = :service)
              AND (:stackHash IS NULL OR e.stackHash = :stackHash)
            ORDER BY e.occurredAt DESC
            """)
    Page<ErrorEvent> search(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to,
                             @Param("service") String service, @Param("stackHash") String stackHash, Pageable pageable);
}
