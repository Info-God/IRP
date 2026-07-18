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
            # 30s, not 10s: search_runbooks routes through irp-core, which calls back into
            # this service's own /v1/embeddings to embed the query - CPU-bound sentence-
            # transformers inference plus the round trip can take longer than the other,
            # much cheaper agent endpoints this client also calls.
            timeout=30.0,
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

    def search_runbooks(self, query: str, top_k: int) -> list[dict[str, Any]]:
        response = self._client.post("/api/v1/agent/runbooks/search", json={"query": query, "topK": top_k})
        response.raise_for_status()
        return response.json()

    def start_agent_run(self, incident_id: str, model: str) -> str:
        response = self._client.post(f"/api/v1/agent/incidents/{incident_id}/runs", json={"model": model})
        response.raise_for_status()
        return response.json()["id"]

    def add_agent_step(self, run_id: str, tool_name: str, tool_input: Any, tool_output: Any) -> None:
        response = self._client.post(f"/api/v1/agent/runs/{run_id}/steps", json={
            "toolName": tool_name, "toolInput": tool_input, "toolOutput": tool_output,
        })
        response.raise_for_status()

    def finish_agent_run(self, run_id: str, status: str, token_usage: dict[str, Any]) -> None:
        response = self._client.patch(f"/api/v1/agent/runs/{run_id}", json={
            "status": status, "tokenUsage": token_usage,
        })
        response.raise_for_status()

    def _get_items(self, path: str, params: dict[str, Any]) -> list[dict[str, Any]]:
        response = self._client.get(path, params=params)
        response.raise_for_status()
        return response.json()["items"]

    def close(self) -> None:
        self._client.close()
