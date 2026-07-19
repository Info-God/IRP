from unittest.mock import patch

from fastapi.testclient import TestClient

from app.config import settings
from app.main import app

client = TestClient(app)


def test_rejects_missing_internal_token():
    response = client.post("/v1/embeddings", json={"texts": ["hello"]})
    assert response.status_code == 401


def test_rejects_wrong_internal_token():
    response = client.post("/v1/embeddings", json={"texts": ["hello"]}, headers={"X-Internal-Token": "wrong"})
    assert response.status_code == 401


@patch("app.api.embeddings.embed_texts")
def test_accepts_correct_token_and_returns_embeddings(mock_embed_texts):
    mock_embed_texts.return_value = [[0.1, 0.2, 0.3]]

    response = client.post("/v1/embeddings", json={"texts": ["hello"]},
                            headers={"X-Internal-Token": settings.internal_token})

    assert response.status_code == 200
    assert response.json() == {"embeddings": [[0.1, 0.2, 0.3]]}
    mock_embed_texts.assert_called_once_with(["hello"])
