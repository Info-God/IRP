import json
import logging
from typing import Any

from groq import BadRequestError, Groq

from app.agent.prompts import build_system_prompt, build_task_prompt
from app.agent.tools import TOOL_DEFINITIONS, ToolExecutor
from app.config import Settings
from app.core_client import IrpCoreClient
from app.guardrails import cap_confidence_if_ungrounded
from app.schemas import InvestigationResult

logger = logging.getLogger(__name__)

MAX_MALFORMED_CALL_RETRIES = 2


class InvestigationOutcome:
    def __init__(self, result: InvestigationResult | None, steps: list[dict[str, Any]], error: str | None = None):
        self.result = result
        self.steps = steps
        self.error = error


def _to_groq_tools() -> list[dict[str, Any]]:
    """Groq's chat completions API is OpenAI-compatible: tools are wrapped in a
    {"type": "function", "function": {...}} envelope rather than Anthropic's flat
    {name, description, input_schema} shape. Only the envelope differs - the tool
    definitions themselves (and ToolExecutor's dispatch) stay provider-agnostic."""
    return [
        {
            "type": "function",
            "function": {
                "name": tool["name"],
                "description": tool["description"],
                "parameters": tool["input_schema"],
            },
        }
        for tool in TOOL_DEFINITIONS
    ]


def run_investigation(settings: Settings, core_client: IrpCoreClient, incident_id: str) -> InvestigationOutcome:
    incident_detail = core_client.get_incident(incident_id)
    incident = incident_detail["incident"]

    executor = ToolExecutor(core_client)
    client = Groq(api_key=settings.groq_api_key)
    groq_tools = _to_groq_tools()

    messages: list[dict[str, Any]] = [
        {"role": "system", "content": build_system_prompt(incident)},
        {"role": "user", "content": build_task_prompt(incident)},
    ]

    final_result: InvestigationResult | None = None
    malformed_call_retries = 0

    for _ in range(settings.max_tool_iterations):
        try:
            response = client.chat.completions.create(
                model=settings.groq_model,
                messages=messages,
                tools=groq_tools,
                tool_choice="auto",
                max_tokens=4096,
            )
        except BadRequestError as exc:
            # Open-weight tool calling isn't as reliable as Claude/GPT - Llama 3.3 on Groq
            # occasionally emits an argument shape Groq's own schema check rejects (e.g. a
            # list instead of an object) before it ever reaches our code. Nudge and retry a
            # bounded number of times rather than letting one bad generation kill the run.
            malformed_call_retries += 1
            logger.warning("Groq rejected a malformed tool call (attempt %d): %s", malformed_call_retries, exc)
            if malformed_call_retries > MAX_MALFORMED_CALL_RETRIES:
                return InvestigationOutcome(
                    result=None, steps=executor.steps,
                    error=f"model produced malformed tool calls repeatedly: {exc}")
            messages.append({
                "role": "user",
                "content": (
                    "Your last tool call was rejected as malformed - the argument must be a single "
                    "JSON object, never a list. Try again, calling exactly one tool at a time."
                ),
            })
            continue

        message = response.choices[0].message
        messages.append(message.model_dump(exclude_none=True))

        if not message.tool_calls:
            break

        for tool_call in message.tool_calls:
            tool_input = json.loads(tool_call.function.arguments)
            output = executor.execute(tool_call.function.name, tool_input)
            if tool_call.function.name == "post_investigation_result":
                final_result = _validate_result(tool_input)
            messages.append({
                "role": "tool",
                "tool_call_id": tool_call.id,
                "content": json.dumps(output, default=str),
            })

        if final_result is not None:
            break

    error = None if final_result is not None else "agent did not produce a result within max_tool_iterations"
    return InvestigationOutcome(result=final_result, steps=executor.steps, error=error)


def _validate_result(raw: dict[str, Any]) -> InvestigationResult:
    result = InvestigationResult.model_validate(raw)
    result.confidence = cap_confidence_if_ungrounded(result.confidence, len(result.evidence))
    return result
