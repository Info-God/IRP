-- Narrows runbook_chunks.embedding from the placeholder 1536 dimensions (V6, sized for an
-- OpenAI-style embedding model) to 384, matching the local model irp-ai-service actually
-- uses (sentence-transformers/all-MiniLM-L6-v2). Safe: RAG was a stub until now, so this
-- table has never been written to - dropping and re-adding the column loses no data.
-- Also drops the NOT NULL constraint on storage_path: runbooks are uploaded as pasted text
-- in this phase, not files, so there is no path to store yet.

DROP INDEX idx_runbook_chunks_embedding;
ALTER TABLE runbook_chunks DROP COLUMN embedding;
ALTER TABLE runbook_chunks ADD COLUMN embedding VECTOR(384) NOT NULL;
CREATE INDEX idx_runbook_chunks_embedding ON runbook_chunks USING hnsw (embedding vector_cosine_ops);

ALTER TABLE runbooks ALTER COLUMN storage_path DROP NOT NULL;
