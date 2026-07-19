# irp-ai-service — AI agent + RAG service

Investigates incidents created in irp-core: pulls recent errors, logs, and deployments for the
incident's service, semantically searches uploaded runbooks, and posts a structured root-cause
suggestion back to irp-core for human approval. Never executes actions - see irp-core's
`AgentSuggestionController` for the human-approval gate.

irp-core calls this service automatically the moment an incident is created (or auto-created
from a repeated-error pattern) - no manual trigger needed. See the [root README](../README.md)
for the full request flow across all four modules.

## Run it

```bash
pip install -r requirements.txt
cp .env.example .env   # fill in IRP_CORE_API_KEY (an agent-scoped API key from irp-core) and GROQ_API_KEY
uvicorn app.main:app --reload --port 8000
```

irp-core calls `POST /v1/investigations` (see `app/api/investigations.py`) with the internal
token configured above; this service then calls back into irp-core's `/api/v1/agent/**` endpoints
(the same auth mechanism the Java SDK uses - a project-scoped `X-API-Key`) to read logs/errors/
deployments and post the resulting suggestion.

## What's here

- Tool-calling investigation loop (`app/agent/orchestrator.py`) using Groq's free-tier,
  OpenAI-compatible chat completions API (`llama-3.3-70b-versatile` by default), with
  `search_errors`, `search_logs`, `get_recent_deployments`, `search_runbooks`, and
  `post_investigation_result` as tools. Every tool call is reported back to irp-core
  (`POST /api/v1/agent/runs`, `/runs/{id}/steps`, `PATCH /runs/{id}`) so the dashboard's
  Agent Trace tab shows exactly what the agent searched and why, not just its final answer.
- Guardrails (`app/guardrails.py`): metadata/message redaction before anything reaches the LLM,
  and confidence capping when the model's answer isn't grounded in retrieved evidence.
- Real RAG: `app/rag/embeddings.py` runs a local `sentence-transformers/all-MiniLM-L6-v2`
  model (free, no API key, pre-warmed at startup) and exposes it over `POST /v1/embeddings`.
  `app/rag/retriever.py` calls back into irp-core's `POST /api/v1/agent/runbooks/search`,
  which does the actual pgvector similarity query - this service never touches the database
  directly. Upload a runbook from the dashboard's Runbooks page and it becomes searchable
  within one embedding round trip.
- No direct database access anywhere else either - every read/write goes through irp-core's
  authenticated REST API, preserving the tenancy and audit invariants already enforced there.
- `tests/` - 34 pytest tests covering guardrails, tool dispatch, the orchestrator's loop
  (happy path, max-iterations-exhausted, and the Llama malformed-tool-call retry path), HTTP
  wire format (via `respx`), and both API routes. Everything is mocked - no live services,
  no API keys, no downloaded model - so `pytest` runs in a few seconds. See `.github/workflows/ci.yml`
  at the repo root.
