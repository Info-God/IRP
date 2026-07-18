package com.irp.core.agent.runbook.dto;

import com.irp.core.agent.runbook.RunbookChunkStore;

import java.util.UUID;

public record RunbookChunkResponse(
        UUID id,
        UUID runbookId,
        int chunkIndex,
        String content
) {
    public static RunbookChunkResponse from(RunbookChunkStore.RunbookChunkMatch match) {
        return new RunbookChunkResponse(match.id(), match.runbookId(), match.chunkIndex(), match.content());
    }
}
