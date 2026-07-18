"""Real pgvector-backed runbook retrieval. Embedding stays local to this service
(app/rag/embeddings.py); the similarity search itself runs in irp-core over its own
Postgres connection via POST /api/v1/agent/runbooks/search, so this service never touches
the database directly - same "everything through irp-core's audited API" boundary every
other tool in app/agent/tools.py already follows."""

from typing import Any

from app.core_client import IrpCoreClient


def search_runbooks(core_client: IrpCoreClient, query: str, top_k: int = 5) -> list[dict[str, Any]]:
    return core_client.search_runbooks(query, top_k)
