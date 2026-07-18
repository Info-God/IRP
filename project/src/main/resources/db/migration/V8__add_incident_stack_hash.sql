-- Lets an incident be linked to the error stack hash that caused it (already computed on
-- ingest, see IngestionService.stackHash()), so the new alert-grouping job can tell whether
-- a repeated error already has an open incident before creating a duplicate one.

ALTER TABLE incidents ADD COLUMN stack_hash VARCHAR(64);

CREATE INDEX idx_incidents_project_stack_hash ON incidents (project_id, stack_hash);
