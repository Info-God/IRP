package com.irp.core.agent.runbook;

import com.irp.core.agent.runbook.dto.CreateRunbookRequest;
import com.irp.core.common.audit.AuditService;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RunbookService {

    private final RunbookRepository runbookRepository;
    private final RunbookChunkStore runbookChunkStore;
    private final RunbookPersister runbookPersister;
    private final EmbeddingClient embeddingClient;
    private final ProjectService projectService;
    private final AuditService auditService;

    /**
     * Deliberately not @Transactional itself: embedding the chunks is an outbound HTTP call to
     * irp-ai-service, and a DB transaction should never sit open across a network call to
     * another service. The runbook row and its chunks are instead committed together by
     * {@link RunbookPersister}, once every embedding has already come back successfully.
     */
    public RunbookWithChunkCount createRunbook(UUID organizationId, UUID projectId, String actor, CreateRunbookRequest request) {
        projectService.getProject(organizationId, projectId);

        List<String> chunks = RunbookChunker.chunk(request.content());
        List<float[]> embeddings = embeddingClient.embed(chunks);

        Runbook runbook = runbookPersister.persist(projectId, actor, request.title(), chunks, embeddings);

        auditService.record(organizationId, projectId, actor, "RUNBOOK_CREATED", "Runbook", runbook.getId().toString(),
                Map.of("title", runbook.getTitle(), "chunkCount", chunks.size()));

        return new RunbookWithChunkCount(runbook, chunks.size());
    }

    @Transactional(readOnly = true)
    public List<RunbookWithChunkCount> listRunbooks(UUID organizationId, UUID projectId) {
        projectService.getProject(organizationId, projectId);
        return runbookRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(runbook -> new RunbookWithChunkCount(runbook, runbookChunkStore.countByRunbookId(runbook.getId())))
                .toList();
    }

    /**
     * organizationId is validated here, not left to the caller: today the only caller
     * (AgentController) passes an API-key principal's own projectId, which is already
     * un-spoofable, but the same tenancy guarantee every other method in this class
     * provides shouldn't silently depend on that staying true for whichever caller is
     * added next.
     */
    public List<RunbookChunkStore.RunbookChunkMatch> search(UUID organizationId, UUID projectId, String query, int topK) {
        projectService.getProject(organizationId, projectId);
        float[] queryEmbedding = embeddingClient.embedOne(query);
        return runbookChunkStore.search(projectId, queryEmbedding, topK);
    }

    public record RunbookWithChunkCount(Runbook runbook, int chunkCount) {
    }
}
