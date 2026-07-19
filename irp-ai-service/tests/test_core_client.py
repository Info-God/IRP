import json

import httpx
import pytest
import respx
from httpx import Response

from app.config import Settings
from app.core_client import IrpCoreClient

BASE_URL = "http://test-core"


def make_client() -> IrpCoreClient:
    settings = Settings(irp_core_base_url=BASE_URL, irp_core_api_key="test-key")
    return IrpCoreClient(settings)


def test_missing_api_key_raises_at_construction():
    settings = Settings(irp_core_base_url=BASE_URL, irp_core_api_key="")
    with pytest.raises(RuntimeError):
        IrpCoreClient(settings)


@respx.mock
def test_search_errors_sends_api_key_header_and_query_params():
    route = respx.get(f"{BASE_URL}/api/v1/agent/errors").mock(
        return_value=Response(200, json={"items": [{"id": "err-1"}]}))

    result = make_client().search_errors(service="checkout", from_time="t0", to_time="t1")

    assert result == [{"id": "err-1"}]
    request = route.calls.last.request
    assert request.headers["X-API-Key"] == "test-key"
    assert request.url.params["service"] == "checkout"


@respx.mock
def test_search_logs_omits_level_param_when_not_given():
    route = respx.get(f"{BASE_URL}/api/v1/agent/logs").mock(return_value=Response(200, json={"items": []}))

    make_client().search_logs(service="checkout", level=None, from_time="t0", to_time="t1")

    assert "level" not in route.calls.last.request.url.params


@respx.mock
def test_get_incident_raises_on_error_status():
    respx.get(f"{BASE_URL}/api/v1/agent/incidents/bad-id").mock(return_value=Response(404))

    with pytest.raises(httpx.HTTPStatusError):
        make_client().get_incident("bad-id")


@respx.mock
def test_post_suggestion_sends_json_body():
    route = respx.post(f"{BASE_URL}/api/v1/agent/incidents/inc-1/suggestions").mock(
        return_value=Response(201, json={"id": "sug-1"}))

    result = make_client().post_suggestion("inc-1", {"rootCause": "x"})

    assert result == {"id": "sug-1"}
    assert json.loads(route.calls.last.request.content) == {"rootCause": "x"}


@respx.mock
def test_search_runbooks_sends_query_and_top_k():
    route = respx.post(f"{BASE_URL}/api/v1/agent/runbooks/search").mock(
        return_value=Response(200, json=[{"id": "chunk-1"}]))

    result = make_client().search_runbooks("checkout npe", 3)

    assert result == [{"id": "chunk-1"}]
    assert json.loads(route.calls.last.request.content) == {"query": "checkout npe", "topK": 3}


@respx.mock
def test_start_agent_run_returns_run_id():
    respx.post(f"{BASE_URL}/api/v1/agent/incidents/inc-1/runs").mock(
        return_value=Response(201, json={"id": "run-1"}))

    assert make_client().start_agent_run("inc-1", "llama-3.3") == "run-1"


@respx.mock
def test_add_agent_step_posts_tool_name_input_and_output():
    route = respx.post(f"{BASE_URL}/api/v1/agent/runs/run-1/steps").mock(return_value=Response(201, json={}))

    make_client().add_agent_step("run-1", "search_errors", {"service": "checkout"}, [{"id": "err-1"}])

    body = json.loads(route.calls.last.request.content)
    assert body == {"toolName": "search_errors", "toolInput": {"service": "checkout"}, "toolOutput": [{"id": "err-1"}]}


@respx.mock
def test_finish_agent_run_patches_status_and_token_usage():
    route = respx.patch(f"{BASE_URL}/api/v1/agent/runs/run-1").mock(return_value=Response(200, json={}))

    make_client().finish_agent_run("run-1", "SUCCEEDED", {"total_tokens": 100})

    body = json.loads(route.calls.last.request.content)
    assert body == {"status": "SUCCEEDED", "tokenUsage": {"total_tokens": 100}}
