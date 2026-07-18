import logging

from fastapi import FastAPI

from app.api.investigations import router as investigations_router

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")

app = FastAPI(
    title="irp-ai-service",
    description="AI agent + RAG service for the Agentic AI Incident Response Platform (Phase 3)",
)

app.include_router(investigations_router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
