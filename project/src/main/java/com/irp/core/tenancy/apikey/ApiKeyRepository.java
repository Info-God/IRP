package com.irp.core.tenancy.apikey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<ApiKey> findByIdAndProjectId(UUID id, UUID projectId);
}
