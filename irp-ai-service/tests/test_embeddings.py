from unittest.mock import MagicMock, patch

import numpy as np

from app.rag.embeddings import embed_texts


def test_embed_texts_returns_empty_list_for_empty_input_without_loading_the_model():
    assert embed_texts([]) == []


@patch("app.rag.embeddings._model")
def test_embed_texts_normalizes_and_converts_to_plain_python_lists(mock_model_fn):
    mock_model = MagicMock()
    mock_model.encode.return_value = np.array([[0.1, 0.2], [0.3, 0.4]])
    mock_model_fn.return_value = mock_model

    result = embed_texts(["a", "b"])

    assert result == [[0.1, 0.2], [0.3, 0.4]]
    assert all(isinstance(v, float) for row in result for v in row)
    mock_model.encode.assert_called_once_with(["a", "b"], normalize_embeddings=True)
