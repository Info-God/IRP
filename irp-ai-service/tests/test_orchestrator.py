import json
from types import SimpleNamespace
from unittest.mock import Mock, patch

import httpx
from groq import BadRequestError

from app.agent.orchestrator import run_investigation
from app.config import Settings


def make_settings(max_tool_iterations: int = 8) -> Settings:
    return Settings(
        groq_api_key="test-groq-key",
        groq_model="test-model",
        max_tool_iterations=max_tool_iterations,
        irp_core_base_url="http://test-core",
        irp_core_api_key="test-key",
    )


def make_core_client() -> Mock:
    core_client = Mock()
    core_client.get_incident.return_value = {
        "incident": {
            "id": "inc-1",
            "projectId": "proj-1",
            "title": "Checkout NPE",
            "service": "checkout",
            "severity": "HIGH",
            "openedAt": "2026-07-18T10:00:00Z",
            "description": "Seen right after deploy",
        },
    }
    core_client.start_agent_run.return_value = "run-1"
    return core_client


def make_tool_call(call_id: str, name: str, arguments: dict) -> SimpleNamespace:
    function = SimpleNamespace(name=name, arguments=json.dumps(arguments))
    return SimpleNamespace(id=call_id, function=function)


def make_groq_response(tool_calls: list, usage=(10, 5, 15)) -> SimpleNamespace:
    message = Mock()
    message.tool_calls = tool_calls
    message.model_dump.return_value = {"role": "assistant"}
    usage_obj = SimpleNamespace(prompt_tokens=usage[0], completion_tokens=usage[1], total_tokens=usage[2])
    return SimpleNamespace(choices=[SimpleNamespace(message=message)], usage=usage_obj)


@patch("app.agent.orchestrator.Groq")
def test_happy_path_reports_result_and_finishes_run_as_succeeded(mock_groq_cls):
    core_client = make_core_client()
    core_client.search_errors.return_value = [{"id": "err-1", "message": "npe"}]

    search_call = make_tool_call("call-1", "search_errors", {"service": "checkout", "from_time": "t0", "to_time": "t1"})
    result_call = make_tool_call("call-2", "post_investigation_result", {
        "root_cause": "missing null check",
        "confidence": 0.8,
        "evidence": [{"type": "error_event", "id": "err-1", "excerpt": "npe"}],
        "recommended_actions": [{"description": "add null check", "risk_level": "LOW"}],
    })
    mock_groq_cls.return_value.chat.completions.create.side_effect = [
        make_groq_response([search_call]),
        make_groq_response([result_call]),
    ]

    outcome = run_investigation(make_settings(), core_client, "inc-1")

    assert outcome.error is None
    assert outcome.result.root_cause == "missing null check"
    assert outcome.result.confidence == 0.8

    core_client.start_agent_run.assert_called_once_with("inc-1", "test-model")
    assert core_client.add_agent_step.call_count == 2
    core_client.add_agent_step.assert_any_call(
        "run-1", "search_errors", {"service": "checkout", "from_time": "t0", "to_time": "t1"},
        [{"id": "err-1", "message": "npe"}])

    core_client.finish_agent_run.assert_called_once()
    finish_call_args = core_client.finish_agent_run.call_args[0]
    assert finish_call_args[0] == "run-1"
    assert finish_call_args[1] == "SUCCEEDED"
    assert finish_call_args[2] == {"prompt_tokens": 20, "completion_tokens": 10, "total_tokens": 30}


@patch("app.agent.orchestrator.Groq")
def test_finishes_run_as_failed_when_max_iterations_exhausted_without_a_result(mock_groq_cls):
    core_client = make_core_client()
    core_client.search_errors.return_value = []
    stall_call = make_tool_call("call-1", "search_errors", {"service": "checkout", "from_time": "t0", "to_time": "t1"})
    mock_groq_cls.return_value.chat.completions.create.return_value = make_groq_response([stall_call])

    outcome = run_investigation(make_settings(max_tool_iterations=2), core_client, "inc-1")

    assert outcome.result is None
    assert "max_tool_iterations" in outcome.error
    core_client.finish_agent_run.assert_called_once_with(
        "run-1", "FAILED", {"prompt_tokens": 20, "completion_tokens": 10, "total_tokens": 30})


@patch("app.agent.orchestrator.Groq")
def test_stops_and_finishes_as_failed_after_repeated_malformed_tool_calls(mock_groq_cls):
    core_client = make_core_client()
    bad_response = httpx.Response(400, request=httpx.Request("POST", "https://api.groq.com"))
    malformed_error = BadRequestError("malformed tool call", response=bad_response, body=None)
    mock_groq_cls.return_value.chat.completions.create.side_effect = malformed_error

    outcome = run_investigation(make_settings(), core_client, "inc-1")

    assert outcome.result is None
    assert "malformed tool calls" in outcome.error
    core_client.finish_agent_run.assert_called_once_with("run-1", "FAILED", {
        "prompt_tokens": 0, "completion_tokens": 0, "total_tokens": 0,
    })
