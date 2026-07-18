package com.irp.core.agent.runbook;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * runbook_chunks has a pgvector `vector(384)` column, which Hibernate has no built-in mapping
 * for. Rather than pull in an extra ORM-vector-type dependency, this talks to that one table
 * directly over JDBC: the embedding is sent as a "[0.1,0.2,...]" text literal and cast with
 * an explicit `::vector` in the SQL, which is the standard way to bind pgvector values without
 * driver-level vector support.
 */
@Repository
public class RunbookChunkStore {

    private final JdbcTemplate jdbcTemplate;

    public RunbookChunkStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertChunk(UUID runbookId, UUID projectId, int chunkIndex, String content, float[] embedding) {
        jdbcTemplate.update("""
                INSERT INTO runbook_chunks (runbook_id, project_id, chunk_index, content, embedding)
                VALUES (?, ?, ?, ?, ?::vector)
                """, runbookId, projectId, chunkIndex, content, toVectorLiteral(embedding));
    }

    /** Nearest-neighbour search by cosine distance ({@code <=>}), scoped to one project. */
    public List<RunbookChunkMatch> search(UUID projectId, float[] queryEmbedding, int topK) {
        return jdbcTemplate.query("""
                SELECT id, runbook_id, chunk_index, content
                FROM runbook_chunks
                WHERE project_id = ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new RunbookChunkMatch(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("runbook_id")),
                        rs.getInt("chunk_index"),
                        rs.getString("content")),
                projectId, toVectorLiteral(queryEmbedding), topK);
    }

    public int countByRunbookId(UUID runbookId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM runbook_chunks WHERE runbook_id = ?", Integer.class, runbookId);
        return count != null ? count : 0;
    }

    private static String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder(embedding.length * 8).append('[');
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(embedding[i]);
        }
        return sb.append(']').toString();
    }

    public record RunbookChunkMatch(UUID id, UUID runbookId, int chunkIndex, String content) {
    }
}
