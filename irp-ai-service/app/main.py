import logging

from fastapi import FastAPI

from app.api.embeddings import router as embeddings_router
from app.api.investigations import router as investigations_router
from app.rag.embeddings import _model as load_embedding_model

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")

app = FastAPI(
    title="irp-ai-service",
    description="AI agent + RAG service for the Agentic AI Incident Response Platform (Phase 3)",
)

app.include_router(investigations_router)
app.include_router(embeddings_router)


@app.on_event("startup")
def _warm_embedding_model() -> None:
    # Loads the sentence-transformers model into memory at process startup instead of on
    # the first real request - otherwise that first request (often mid-investigation, on a
    # tight tool-call timeout) pays for both the model load and the embedding.
    load_embedding_model()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
