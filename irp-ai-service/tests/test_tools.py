from unittest.mock import Mock

from app.agent.tools import ToolExecutor


def make_executor():
    core_client = Mock()
    return ToolExecutor(core_client), core_client


def test_search_errors_redacts_message_and_metadata():
    executor, core_client = make_executor()
    core_client.search_errors.return_value = [
        {"id": "err-1", "message": "token: abc123 leaked", "metadata": {"apiKey": "secret"}},
    ]

    result = executor.execute("search_errors", {"service": "checkout", "from_time": "t0", "to_time": "t1"})

    assert "abc123" not in result[0]["message"]
    assert result[0]["metadata"]["apiKey"] == "***REDACTED***"
    core_client.search_errors.assert_called_once_with(service="checkout", from_time="t0", to_time="t1")


def test_search_logs_passes_optional_level():
    executor, core_client = make_executor()
    core_client.search_logs.return_value = []

    executor.execute("search_logs", {"service": "checkout", "from_time": "t0", "to_time": "t1", "level": "ERROR"})

    core_client.search_logs.assert_called_once_with(service="checkout", level="ERROR", from_time="t0", to_time="t1")


def test_get_recent_deployments_calls_core_client():
    executor, core_client = make_executor()
    core_client.search_deployments.return_value = [{"id": "dep-1"}]

    result = executor.execute("get_recent_deployments", {"service": "checkout", "from_time": "t0", "to_time": "t1"})

    assert result == [{"id": "dep-1"}]


def test_search_runbooks_passes_query_and_top_k_through_to_core_client():
    executor, core_client = make_executor()
    core_client.search_runbooks.return_value = [{"id": "chunk-1"}]

    result = executor.execute("search_runbooks", {"query": "checkout npe", "top_k": 3})

    core_client.search_runbooks.assert_called_once_with("checkout npe", 3)
    assert result == [{"id": "chunk-1"}]


def test_search_runbooks_defaults_top_k_to_5():
    executor, core_client = make_executor()
    core_client.search_runbooks.return_value = []

    executor.execute("search_runbooks", {"query": "checkout npe"})

    core_client.search_runbooks.assert_called_once_with("checkout npe", 5)


def test_post_investigation_result_acknowledges():
    executor, _ = make_executor()

    result = executor.execute("post_investigation_result", {"root_cause": "x", "confidence": 0.5})

    assert result == {"acknowledged": True}


def test_unknown_tool_returns_error_without_raising():
    executor, _ = make_executor()

    result = executor.execute("delete_everything", {})

    assert "error" in result


def test_every_call_is_recorded_in_steps_in_order():
    executor, core_client = make_executor()
    core_client.search_errors.return_value = []
    core_client.search_deployments.return_value = []

    executor.execute("search_errors", {"service": "a", "from_time": "t0", "to_time": "t1"})
    executor.execute("get_recent_deployments", {"service": "a", "from_time": "t0", "to_time": "t1"})

    assert [s["tool_name"] for s in executor.steps] == ["search_errors", "get_recent_deployments"]
