package com.irp.core.agent.runbook.dto;

import com.irp.core.agent.runbook.Runbook;
import com.irp.core.agent.runbook.RunbookService;

import java.time.Instant;
import java.util.UUID;

public record RunbookResponse(
        UUID id,
        UUID projectId,
        String title,
        int version,
        int chunkCount,
        String uploadedBy,
        Instant createdAt
) {
    public static RunbookResponse from(Runbook runbook, int chunkCount) {
        return new RunbookResponse(runbook.getId(), runbook.getProjectId(), runbook.getTitle(),
                runbook.getVersion(), chunkCount, runbook.getUploadedBy(), runbook.getCreatedAt());
    }

    public static RunbookResponse from(RunbookService.RunbookWithChunkCount runbookWithChunkCount) {
        return from(runbookWithChunkCount.runbook(), runbookWithChunkCount.chunkCount());
    }
}
