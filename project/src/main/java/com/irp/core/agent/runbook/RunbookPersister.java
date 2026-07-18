package com.irp.core.agent.runbook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Separated out from {@link RunbookService} so its one method can be {@code @Transactional}
 * without falling into Spring's self-invocation trap: calling an annotated method on `this`
 * from within the same class bypasses the transactional proxy entirely. Going through a
 * distinct bean, as RunbookService does here, keeps the runbook row and all of its chunks
 * committed atomically.
 */
@Component
@RequiredArgsConstructor
class RunbookPersister {

    private final RunbookRepository runbookRepository;
    private final RunbookChunkStore runbookChunkStore;

    @Transactional
    Runbook persist(UUID projectId, String actor, String title, List<String> chunks, List<float[]> embeddings) {
        // saveAndFlush, not save: the chunk inserts right below go through raw JDBC, which
        // Hibernate has no visibility into, so it won't auto-flush the buffered runbook INSERT
        // before them - without an explicit flush the chunk rows fail their FK check because
        // the runbook row isn't in the database yet.
        Runbook runbook = runbookRepository.saveAndFlush(new Runbook(projectId, title, actor));
        for (int i = 0; i < chunks.size(); i++) {
            runbookChunkStore.insertChunk(runbook.getId(), projectId, i, chunks.get(i), embeddings.get(i));
        }
        return runbook;
    }
}
