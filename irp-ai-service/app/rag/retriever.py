"""Stub retriever - no runbook upload endpoint or embedding pipeline exists yet.
irp-core's `runbooks`/`runbook_chunks` tables (pgvector-backed) are laid down and ready,
but nothing writes to them yet. Once runbook ingestion is built, replace this function's
body with a pgvector similarity query; the tool contract below (query, top_k -> chunks)
stays the same so `app/agent/tools.py` doesn't need to change."""

from typing import Any


def search_runbooks(query: str, top_k: int = 5) -> list[dict[str, Any]]:
    return []
