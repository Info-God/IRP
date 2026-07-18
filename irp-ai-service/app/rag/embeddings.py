"""Local embedding model (sentence-transformers/all-MiniLM-L6-v2, 384 dimensions) - free,
no external API key, runs on CPU. The model is loaded lazily and cached at module scope so
it's only ever loaded into memory once per process, not once per request."""

from functools import lru_cache


@lru_cache(maxsize=1)
def _model():
    from sentence_transformers import SentenceTransformer

    # local_files_only: skip the Hugging Face Hub freshness check on every cold start -
    # once the model is cached (first run), there's no need to phone home before each use,
    # and that HTTP round trip was adding multiple seconds to the first embedding request
    # after a restart.
    return SentenceTransformer("all-MiniLM-L6-v2", local_files_only=True)


def embed_texts(texts: list[str]) -> list[list[float]]:
    if not texts:
        return []
    vectors = _model().encode(texts, normalize_embeddings=True)
    return [vector.tolist() for vector in vectors]
