from fastapi import APIRouter, Header, HTTPException, status
from pydantic import BaseModel

from app.config import settings
from app.rag.embeddings import embed_texts

router = APIRouter(prefix="/v1/embeddings", tags=["embeddings"])


class EmbeddingRequest(BaseModel):
    texts: list[str]


class EmbeddingResponse(BaseModel):
    embeddings: list[list[float]]


@router.post("", response_model=EmbeddingResponse)
def create_embeddings(
    request: EmbeddingRequest,
    x_internal_token: str = Header(default=""),
) -> EmbeddingResponse:
    if x_internal_token != settings.internal_token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid internal token")

    return EmbeddingResponse(embeddings=embed_texts(request.texts))
