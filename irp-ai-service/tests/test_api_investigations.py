from unittest.mock import patch

from fastapi.testclient import TestClient

from app.config import settings
from app.main import app

client = TestClient(app)

VALID_BODY = {"incident_id": "inc-1", "project_id": "proj-1", "organization_id": "org-1"}


def test_rejects_missing_internal_token():
    response = client.post("/v1/investigations", json=VALID_BODY)
    assert response.status_code == 401


def test_rejects_wrong_internal_token():
    response = client.post("/v1/investigations", json=VALID_BODY, headers={"X-Internal-Token": "wrong"})
    assert response.status_code == 401


@patch("app.api.investigations._investigate_and_report")
def test_accepts_correct_token_and_schedules_the_investigation(mock_investigate):
    response = client.post("/v1/investigations", json=VALID_BODY,
                            headers={"X-Internal-Token": settings.internal_token})

    assert response.status_code == 202
    assert response.json() == {"status": "accepted"}
    mock_investigate.assert_called_once_with("inc-1")
