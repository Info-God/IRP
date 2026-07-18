from typing import Any

import httpx

from app.config import Settings


class IrpCoreClient:
    """Wraps irp-core's /api/v1/agent/** endpoints - authenticated the same way the
    Java SDK is, via a project-scoped X-API-Key. No direct database access: every
    read and write goes through irp-core's own tenancy/audit-checked API."""

    def __init__(self, settings: Settings):
        if not settings.irp_core_api_key:
            raise RuntimeError("IRP_CORE_API_KEY is not set")
        self._client = httpx.Client(
            base_url=settings.irp_core_base_url.rstrip("/"),
            headers={"X-API-Key": settings.irp_core_api_key},
            timeout=10.0,
        )

    def search_logs(self, service: str, level: str | None, from_time: str, to_time: str) -> list[dict[str, Any]]:
        params: dict[str, Any] = {"service": service, "from": from_time, "to": to_time}
        if level:
            params["level"] = level
        return self._get_items("/api/v1/agent/logs", params)

    def search_errors(self, service: str, from_time: str, to_time: str) -> list[dict[str, Any]]:
        params = {"service": service, "from": from_time, "to": to_time}
        return self._get_items("/api/v1/agent/errors", params)

    def search_deployments(self, service: str, from_time: str, to_time: str) -> list[dict[str, Any]]:
        params = {"service": service, "from": from_time, "to": to_time}
        return self._get_items("/api/v1/agent/deployments", params)

    def get_incident(self, incident_id: str) -> dict[str, Any]:
        response = self._client.get(f"/api/v1/agent/incidents/{incident_id}")
        response.raise_for_status()
        return response.json()

    def post_suggestion(self, incident_id: str, payload: dict[str, Any]) -> dict[str, Any]:
        response = self._client.post(f"/api/v1/agent/incidents/{incident_id}/suggestions", json=payload)
        response.raise_for_status()
        return response.json()

    def _get_items(self, path: str, params: dict[str, Any]) -> list[dict[str, Any]]:
        response = self._client.get(path, params=params)
        response.raise_for_status()
        return response.json()["items"]

    def close(self) -> None:
        self._client.close()
