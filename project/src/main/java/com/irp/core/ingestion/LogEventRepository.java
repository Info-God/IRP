package com.irp.core.ingestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface LogEventRepository extends JpaRepository<LogEvent, UUID> {

    @Query("""
            SELECT l FROM LogEvent l
            WHERE l.projectId = :projectId
              AND l.occurredAt BETWEEN :from AND :to
              AND (:service IS NULL OR l.service = :service)
              AND (:level IS NULL OR l.level = :level)
            ORDER BY l.occurredAt DESC
            """)
    Page<LogEvent> search(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to,
                           @Param("service") String service, @Param("level") LogLevel level, Pageable pageable);
}
