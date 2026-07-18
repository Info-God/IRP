from typing import Any

from app.core_client import IrpCoreClient
from app.guardrails import redact_metadata, redact_text
from app.rag.retriever import search_runbooks

TOOL_DEFINITIONS: list[dict[str, Any]] = [
    {
        "name": "search_errors",
        "description": (
            "Search recent error events for a service in a time window. Call this first "
            "to find repeat exceptions around the incident's opened_at time."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "service": {"type": "string", "description": "Service name to filter by"},
                "from_time": {"type": "string", "description": "ISO 8601 start of window"},
                "to_time": {"type": "string", "description": "ISO 8601 end of window"},
            },
            "required": ["service", "from_time", "to_time"],
        },
    },
    {
        "name": "search_logs",
        "description": (
            "Search recent log events (optionally filtered by level, e.g. ERROR or WARN) "
            "for a service in a time window."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "service": {"type": "string"},
                "level": {"type": "string", "description": "TRACE, DEBUG, INFO, WARN, or ERROR"},
                "from_time": {"type": "string"},
                "to_time": {"type": "string"},
            },
            "required": ["service", "from_time", "to_time"],
        },
    },
    {
        "name": "get_recent_deployments",
        "description": (
            "List recent deployments for a service in a time window - useful for "
            "correlating the incident with a recent release."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "service": {"type": "string"},
                "from_time": {"type": "string"},
                "to_time": {"type": "string"},
            },
            "required": ["service", "from_time", "to_time"],
        },
    },
    {
        "name": "search_runbooks",
        "description": "Semantic search over uploaded runbooks for remediation guidance relevant to a query.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {"type": "string"},
                "top_k": {"type": "integer", "default": 5},
            },
            "required": ["query"],
        },
    },
    {
        "name": "post_investigation_result",
        "description": "Submit your final investigation result. Call this exactly once, as the last step.",
        "input_schema": {
            "type": "object",
            "properties": {
                "root_cause": {"type": "string"},
                "confidence": {"type": "number", "minimum": 0, "maximum": 1},
                "evidence": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "type": {
                                "type": "string",
                                "enum": ["error_event", "log_event", "deployment", "runbook"],
                            },
                            "id": {"type": "string"},
                            "excerpt": {"type": "string"},
                        },
                        "required": ["type", "id", "excerpt"],
                    },
                },
                "recommended_actions": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "description": {"type": "string"},
                            "risk_level": {"type": "string", "enum": ["LOW", "MEDIUM", "HIGH"]},
                        },
                        "required": ["description", "risk_level"],
                    },
                },
            },
            "required": ["root_cause", "confidence", "evidence", "recommended_actions"],
        },
    },
]


class ToolExecutor:
    """Dispatches tool_use blocks to irp-core reads or the (stub) RAG retriever. Every
    call and its result is appended to `steps`, which becomes the agent_steps audit trail."""

    def __init__(self, core_client: IrpCoreClient):
        self._core_client = core_client
        self.steps: list[dict[str, Any]] = []

    def execute(self, tool_name: str, tool_input: dict[str, Any]) -> Any:
        handler = getattr(self, f"_tool_{tool_name}", None)
        result = {"error": f"unknown tool {tool_name}"} if handler is None else handler(tool_input)
        self.steps.append({"tool_name": tool_name, "tool_input": tool_input, "tool_output": result})
        return result

    def _tool_search_errors(self, tool_input: dict[str, Any]) -> Any:
        events = self._core_client.search_errors(
            service=tool_input["service"], from_time=tool_input["from_time"], to_time=tool_input["to_time"])
        return [self._redact_event(e) for e in events]

    def _tool_search_logs(self, tool_input: dict[str, Any]) -> Any:
        events = self._core_client.search_logs(
            service=tool_input["service"], level=tool_input.get("level"),
            from_time=tool_input["from_time"], to_time=tool_input["to_time"])
        return [self._redact_event(e) for e in events]

    def _tool_get_recent_deployments(self, tool_input: dict[str, Any]) -> Any:
        return self._core_client.search_deployments(
            service=tool_input["service"], from_time=tool_input["from_time"], to_time=tool_input["to_time"])

    def _tool_search_runbooks(self, tool_input: dict[str, Any]) -> Any:
        return search_runbooks(tool_input["query"], tool_input.get("top_k", 5))

    def _tool_post_investigation_result(self, tool_input: dict[str, Any]) -> Any:
        return {"acknowledged": True}

    @staticmethod
    def _redact_event(event: dict[str, Any]) -> dict[str, Any]:
        event = dict(event)
        if event.get("message"):
            event["message"] = redact_text(event["message"])
        if "metadata" in event:
            event["metadata"] = redact_metadata(event.get("metadata"))
        return event
